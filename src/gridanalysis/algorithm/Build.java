/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Level;
import gridanalysis.gridclasses.Range;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.MEngine;
import gridanalysis.jfx.shape.MCellInfo;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.utilities.list.ObjectList;
import static java.lang.Math.cbrt;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 *
 * @author user
 */
public class Build extends GridAbstracts{
    MEngine engine;    
    Hagrid hagrid;
   
    public Build(MEngine engine, Hagrid hagrid)
    {
        this.engine = engine;
        this.hagrid = hagrid;
    }
    
    /// Computes the range of cells that intersect the given box
    public Range compute_range(Vec2i dims, BBox grid_bb, BBox obj_bb) {
        
        Vec2f inv = new Vec2f(dims).div(grid_bb.extents());
             
        int lx = max((int)((obj_bb.min.x - grid_bb.min.x) * inv.x), 0);
        int ly = max((int)((obj_bb.min.y - grid_bb.min.y) * inv.y), 0);
        int hx = min((int)((obj_bb.max.x - grid_bb.min.x) * inv.x), dims.x - 1);
        int hy = min((int)((obj_bb.max.y - grid_bb.min.y) * inv.y), dims.y - 1);
        
        return new Range(lx, ly, hx, hy);
    }
    
    /// Computes grid dimensions based on the formula by Cleary et al.
    public Vec2i compute_grid_dims(BBox bb, int num_prims, float density) {
        Vec2f extents = bb.extents();
        float volume = extents.x * extents.y;
        float ratio = (float) cbrt(density * num_prims / volume);
        return Vec2i.max(new Vec2i(1), new Vec2i(
                extents.x * ratio,
                extents.y * ratio));        
    }
    
    public void compute_bboxes(
            Tri[] prims,
            ObjectList<BBox> bboxes,
            int num_prims) 
    {
        for(int i = 0; i<num_prims; i++)
            bboxes.set(i, prims[i].bbox());
    }
    
    /// Compute an over-approximation of the number of references
    /// that are going to be generated during reference emission
    public void count_new_refs(
            ObjectList<BBox>  bboxes,
            IntegerList counts,
            int num_prims) {
        
        for(int id = 0; id<num_prims; id++)
        {
            if (id >= num_prims) return;
            
            BBox ref_bb = bboxes.get(id);//load_bbox(bboxes + id);
            Range range  = compute_range(hagrid.grid_dims, hagrid.grid_bbox, ref_bb);
            counts.set(id, Math.max(0, range.size()));               
        }    
    }
    
    /// Given a position on the virtual grid, return the corresponding top-level cell index
    public int top_level_cell(Vec2i pos) {
        return (pos.x >> hagrid.grid_shift) + hagrid.grid_dims.x * ((pos.y >> hagrid.grid_shift));
    }
        
    /// Update the logarithm of the sub-level resolution for top-level cells (after a new subdivision level)
    public void update_log_dims(IntegerList log_dims, int num_top_cells) {
        for(int id = 0; id<num_top_cells; id++)
        {
            if (id >= num_top_cells) return;            
            log_dims.set(id, max(0, log_dims.get(id) - 1));            
        }
        
    }
    
    /// Mark references that are kept so that they can be moved to the beginning of the array
    public void mark_kept_refs(
            IntegerList cell_ids,
            Entry[] entries,
            IntegerList kept_flags,
            int num_refs) {
        for(int id = 0; id<num_refs; id++)
        {            
            if (id >= num_refs) return;

            int cell_id = cell_ids.get(id);  
            
            int value = ((cell_id >= 0) && (entries[cell_id].log_dim == 0)) ? 1 : 0;              
            kept_flags.set(id, value);
        }             
    }
    
    public void count_refs_per_cell(IntegerList cell_ids,
                                    IntegerList refs_per_cell,                                    
                                    int num_refs) {
        for(int id = 0; id<num_refs; id++)
        {
            if (id >= num_refs) return;
            int cell_id = cell_ids.get(id);
            if (cell_id >= 0) 
            {
                //atomicAdd(refs_per_cell + cell_id, 1);
                int v = refs_per_cell.get(cell_id); 
                refs_per_cell.set(cell_id, v + 1);
            }
        }
    }
    
    /// Compute the logarithm of the sub-level resolution for top-level cells
    public void compute_log_dims(
            IntegerList refs_per_cell,
            IntegerList log_dims,
            float snd_density,
            int num_cells) {       
        
        for(int id = 0; id<num_cells; id++)
        {         
            if (id >= num_cells) return;

            Vec2f extents = hagrid.grid_bbox.extents().div(new Vec2f(hagrid.grid_dims));
            BBox bbox = new BBox(new Vec2f(), extents);     
            
            Vec2i dims = compute_grid_dims(bbox, refs_per_cell.get(id), snd_density);            
            int max_dim = max(dims.x, dims.y);
            int log_dim = log2nlz(max_dim); //log2
            log_dim = (1 << log_dim) < max_dim ? log_dim + 1 : log_dim;            
            log_dims.set(id, log_dim);       
            
        }    
    }
    
    /// Update the entries for the one level before the current one
    public void update_entries(
            IntegerList start_cell,
            Entry[] entries,
            int num_cells) {
        for(int id = 0; id<num_cells; id++)
        {            
            if (id >= num_cells) return;

            int start = start_cell.get(id);
            Entry entry = entries[id];

            // If the cell is subdivided, write the first sub-cell index into the current entry
            entry.begin = entry.log_dim != 0 ? start : id;
            
        }
    }
    
    /// Copy the references with an offset, different for each level
    public void copy_refs(
                            IntegerList cell_ids,
                            IntegerList new_cell_ids,
                            int cell_off,
                            int num_kept) {
        for(int id = 0; id<num_kept; id++)
        {        
            if (id >= num_kept) return;
            new_cell_ids.set(id, cell_ids.get(id) + cell_off);
        }
    }
    
    /// Mark the cells that are used as 'kept'
    public void mark_kept_cells(
                                Entry[] entries,
                                IntegerList kept_cells,
                                int num_cells) {        
        for(int id = 0; id<num_cells; id++)
        {
            if (id >= num_cells) return;
            kept_cells.set(id, (entries[id].log_dim == 0) ? 1 : 0);
        }
    }
    
    /// Copy only the cells that are kept to another array of cells
    public void copy_cells(
                            Cell[] cells,
                            IntegerList start_cell,
                            Cell[] new_cells,
                            int cell_off,
                            int num_cells) {
        for(int id = 0; id<num_cells; id++)
        {
            if (id >= num_cells) return;

            Cell cell = cells[id].copy();
            int start = start_cell.get(cell_off + id + 0);
            int end   = start_cell.get(cell_off + id + 1);
            if (start < end) 
                new_cells[start] = cell;                
        }
    }
    
    /// Copy the voxel map entries and remap kept cells to their correct indices
    public void copy_entries(Entry[] entries,
                             IntegerList start_cell,
                             Entry[] new_entries,
                             int cell_off,
                             int next_level_off,
                             int num_cells) {
        for(int id = 0; id<num_cells; id++)
        {
            if (id >= num_cells) return;

            Entry entry = entries[id];
            if (entry.log_dim == 0) {
                // Points to a cell
                entry.begin = start_cell.get(cell_off + entry.begin);
            } else {
                // Points to another entry in the next level
                entry.begin += next_level_off;
            }
            new_entries[id + cell_off] = entry.copy();
        }
    }
    
    /// Remap references so that they map to the correct cells
    public void remap_refs(
                        IntegerList cell_ids,
                        IntegerList start_cell,
                        int num_refs) {
        for(int id = 0; id<num_refs; id++)
        {
            if (id >= num_refs) return;

            cell_ids.set(id, start_cell.get(cell_ids.get(id)));
        }
        
        
    }
    
    /// Sets the cell ranges once the references are sorted by cell
    public void compute_cell_ranges(IntegerList cell_ids, Cell[] cells, int num_refs) {
        
        for(int id = 0; id<num_refs; id++)
        {
            if (id >= num_refs) return;
            
            int cell_id = cell_ids.get(id + 0);
            if (id >= num_refs - 1) {
                cells[cell_id].end = id + 1; //System.out.println(num_refs);                
                return;
            }
            int next_id = cell_ids.get(id + 1);
            
            if (cell_id != next_id) {
                cells[cell_id].end   = id + 1; 
                cells[next_id].begin = id + 1;
                
            }            
        }
    }
    
    
       
    /// Emit the new references by inserting existing ones into the sub-levels
    public void emit_new_refs(
            ObjectList<BBox> bboxes,
            IntegerList start_emit, //this is a prefix sum
            IntegerList new_ref_ids,
            IntegerList new_cell_ids,
            int num_prims){              
        for(int id = 0; id<num_prims; id++)
        {
            int start = start_emit.get(id + 0);
            int end   = start_emit.get(id + 1);
            Range range;
            //Very strange method. In the cuda code, it seems quite complex
            if (start < end) 
            {
                BBox ref_bb = bboxes.get(id);
                range  = compute_range(hagrid.grid_dims, hagrid.grid_bbox, ref_bb);
                             
                int x = range.lx;
                int y = range.ly;               
                int cur = start;
                while (cur < end) 
                {                    
                    new_ref_ids.set(cur, id);
                    new_cell_ids.set(cur, x + hagrid.grid_dims.x * y);
                    cur++; 
                    x++;
                    if (x > range.hx) { x = range.lx; y++; }
                    if (y > range.hy) { y = range.ly;} 
                }                
            }            
        }        
    }
    
    /// Generate cells for the top level
    public void emit_top_cells(Cell[] new_cells, int num_cells) {
        for(int id = 0; id<num_cells; id++)
        {            
            if (id >= num_cells) return;

            int x = id % hagrid.grid_dims.x;
            int y = (id / hagrid.grid_dims.x) % hagrid.grid_dims.y;
            int inc = 1 << hagrid.grid_shift;
            
            x <<= hagrid.grid_shift;
            y <<= hagrid.grid_shift;           

            Cell cell = new Cell();
            cell.min = new Vec2i(x, y);
            cell.max = new Vec2i(x + inc, y + inc);
            cell.begin = 0;
            cell.end   = 0;
            new_cells[id] = cell;
            
        }
    }
    
    /// Generate new cells based on the previous level
    public void emit_new_cells(Entry[] entries,
                               Cell[] cells,
                               Cell[] new_cells,
                               int num_cells) {
        
        for(int id = 0; id<num_cells; id++)
        {
            if (id >= num_cells) return;

            Entry entry = entries[id];
            int log_dim = entry.log_dim;
            if (log_dim == 0) continue;
            

            int start = entry.begin;
            Cell cell = cells[id].copy();
            int min_x = cell.min.x;
            int min_y = cell.min.y;
            int inc = (cell.max.x - cell.min.x) >> 1;
            
            for (int i = 0; i < 4; i++) {
                int x = min_x + (i & 1) * inc;
                int y = min_y + ((i >> 1) & 1) * inc;
                 
                cell.min = new Vec2i(x, y);
                cell.max = new Vec2i(x + inc, y + inc);
                cell.begin = 0;
                cell.end   = 0;
                new_cells[start + i] = cell.copy();    
                
            }
        }
        
        
    }
    
    /// Count the (sub-)dimensions of each cell, based on the array of references
    public void compute_dims(
            IntegerList  cell_ids,
            Cell[] cells,
            IntegerList log_dims,
            Entry[] entries,
            int num_refs) {
        
        for(int id = 0; id<num_refs; id++)
        {            
            if (id >= num_refs) return;

            int cell_id = cell_ids.get(id);
            if (cell_id < 0) continue;
            
            Vec2i cell_min = cells[cell_id].min;           
                        
            int top_cell_id = top_level_cell(cell_min);
            int log_dim = log_dims.get(top_cell_id);
            
            entries[cell_id] = make_entry(min(log_dim, 1), 0);
        }       
    }
    
    public void compute_split_masks(IntegerList cell_ids,
                                    IntegerList ref_ids,
                                    Tri[] prims,
                                    Cell[] cells,
                                    IntegerList split_masks,
                                    int num_split) { 
        
        
        
        for(int id = 0; id<num_split; id++)
        {            
            if (id >= num_split) return;

            int cell_id = cell_ids.get(id);
            if (cell_id < 0) {
                split_masks.set(id, 0);
                continue;
            }
            
            int ref  =  ref_ids.get(id);
            Cell cell = cells[cell_id].copy();              
            Tri prim = prims[ref];

            Vec2f cell_min = hagrid.grid_bbox.min.add(hagrid.cell_size.mul(new Vec2f(cell.min)));
            Vec2f cell_max = hagrid.grid_bbox.min.add(hagrid.cell_size.mul(new Vec2f(cell.max)));
            Vec2f middle = (cell_min.add(cell_max)).mul(0.5f);

            int mask = 0xF;

            // Optimization: Test against half spaces first
            BBox ref_bb = prim.bbox();            
            if (ref_bb.min.x > cell_max.x ||
                ref_bb.max.x < cell_min.x) mask  = 0;
            if (ref_bb.min.x >   middle.x) mask &= 0xAA; //10101010
            if (ref_bb.max.x <   middle.x) mask &= 0x55; //01010101
            if (ref_bb.min.y > cell_max.y ||
                ref_bb.max.y < cell_min.y) mask  = 0;
            if (ref_bb.min.y >   middle.y) mask &= 0xCC; //11001100
            if (ref_bb.max.y <   middle.y) mask &= 0x33; //00110011    
            
            for (int i = __ffs(mask) - 1;;) {
                BBox bbox = new BBox(
                        new Vec2f((i & 1) != 0 ? middle.x : cell_min.x,
                                  (i & 2) != 0 ? middle.y : cell_min.y),
                        new Vec2f((i & 1) != 0 ? cell_max.x : middle.x,
                                  (i & 2) != 0 ? cell_max.y : middle.y));
                if (!Tri.intersect_prim_cell(prim, bbox)) mask &= ~(1 << i);

                // Skip non-intersected children
                int skip = __ffs(mask >> (i + 1));
                if (skip == 0) break;
                i += 1 + (skip - 1);
            }

            split_masks.set(id, mask);
        }
    }
    
    /// Split references according to the given array of split masks
    public void split_refs(
                    IntegerList cell_ids,
                    IntegerList ref_ids,
                    Entry[] entries,
                    IntegerList split_masks,
                    IntegerList start_split,
                    IntegerList new_cell_ids,
                    IntegerList new_ref_ids,
                    int num_split) { 
        
        for(int id = 0; id<num_split; id++)
        {
            if (id >= num_split) return;
            
            int cell_id = cell_ids.get(id); 
            
            //Not in the code
            if(cell_id < 0)
               continue;
            
            int ref = ref_ids.get(id); 
            int begin = entries[cell_id].begin;
            int mask  = split_masks.get(id);
            int start = start_split.get(id);
            while (mask != 0) {
                int child_id = __ffs(mask) - 1;
                mask &= ~(1 << child_id);
                new_ref_ids.set(start, ref);                
                new_cell_ids.set(start, begin + child_id);                 
                start++;
            }
        }
        
    }
    
    /// Filter out references that do not intersect the cell they are in
    public void filter_refs(
            IntegerList cell_ids,
            IntegerList ref_ids,
            Tri[] prims,
            Cell[] cells,
            int num_refs) 
    {        
        
        for(int id = 0; id<num_refs; id++)
        {            
            if (id >= num_refs) return;

            
            Cell cell = cells[cell_ids.get(id)];
            Tri prim = prims[ref_ids.get(id)];
            
            BBox bbox = new BBox(
                    hagrid.grid_bbox.min.add(new Vec2f(cell.min).mul(hagrid.cell_size)),
                    hagrid.grid_bbox.min.add(new Vec2f(cell.max).mul(hagrid.cell_size)));     
            
            boolean intersect = Tri.intersect_prim_cell(prim, bbox);
            if (!intersect) {
                cell_ids.set(id, -1);
                ref_ids.set(id, -1);                
            }                
        }
        
    }
    
    public void first_build_iter(
            float snd_density,
            Tri[] prims, int num_prims,
            ObjectList<BBox> bboxes, BBox grid_bb, Vec2i dims, IntegerList log_dims, 
            int[] grid_shift, ObjectList<Level> levels) 
    {
        int num_top_cells = dims.x * dims.y;
       
        IntegerList start_emit        = new IntegerList(new int[num_prims + 1]);
        IntegerList new_ref_counts    = new IntegerList(new int[num_prims + 1]);
        IntegerList refs_per_cell     = new IntegerList(new int[num_top_cells]);
        
        log_dims.resize(num_top_cells + 1);
                
        count_new_refs(bboxes, new_ref_counts, num_prims);  //new references generated based on how many cells each ref overlaps      
        new_ref_counts.copyTo(start_emit).shiftRight(1); //copy to and shift to the right
        int num_new_refs = start_emit.prefixSum(0, num_prims + 1); 
        
        // We are creating cells and their references of top level
        IntegerList new_ref_ids  = new IntegerList(new int[2 * num_new_refs]); 
        IntegerList new_cell_ids = new_ref_ids.getSubListFrom(num_new_refs);   
        emit_new_refs(bboxes, start_emit, new_ref_ids, new_cell_ids, num_prims); //insert the initial references into the new cells 
        
        // Compute the number of references per cell
        count_refs_per_cell(new_cell_ids, refs_per_cell, num_new_refs); //simple atomic add
        
        // Compute an independent resolution in each of the top-level cells
        compute_log_dims(refs_per_cell, log_dims, snd_density, num_top_cells);
                
        // Find the max sub-level resolution
        grid_shift[0] = log_dims.reduce(0, (a, b) -> Math.max(a, b));
        
        System.out.println(log_dims);
                               
        this.hagrid.cell_size = grid_bb.extents().div(new Vec2f(dims.leftShift(grid_shift[0])));
        this.hagrid.grid_shift = grid_shift[0];
                
        //Emission of the new cells
        Cell[] new_cells   = new Cell[num_top_cells + 0];
        Entry[] new_entries = new Entry[num_top_cells + 1];
        
        emit_top_cells(new_cells, num_top_cells);
        for(int i = 0; i<num_top_cells + 1; i++)
            new_entries[i] = new Entry();
        
        
        // Filter out the references that do not intersect the cell they are in
        // Initially cell intersection was based on primitive bounding box, here we are more precise with primitive intersection with cell
        filter_refs(new_cell_ids, new_ref_ids, prims, new_cells, num_new_refs);
        
        Level level = new Level();
        level.ref_ids   = new_ref_ids;  
        level.cell_ids  = new_cell_ids; 
        level.num_refs  = num_new_refs;   
        level.num_kept  = num_new_refs;   
        level.cells     = new_cells;   
        level.entries   = new_entries;
        level.num_cells = num_top_cells;  
                        
        levels.add(level);       
    }
    
    public boolean build_iter(
                Tri[] prims, int num_prims,
                Vec2i dims, IntegerList log_dims,
                ObjectList<Level> levels,
                int iter)
    {
        IntegerList cell_ids    = levels.back().cell_ids;
        IntegerList ref_ids     = levels.back().ref_ids;
        Cell[] cells            = levels.back().cells;
        Entry[] entries         = levels.back().entries;
        
        int num_top_cells       = dims.x * dims.y;
        int num_refs            = levels.back().num_refs;
        int num_cells           = levels.back().num_cells;

        int cur_level  = levels.size();
        
        IntegerList kept_flags = new IntegerList(new int[num_refs + 1]);
        
        // Find out which cell will be split based on whether it is empty or not and the max depth
        compute_dims(cell_ids, cells, log_dims, entries, num_refs); 
        update_log_dims(log_dims, num_top_cells);        
        mark_kept_refs(cell_ids, entries, kept_flags, num_refs);
        
        // Store the sub-cells starting index in the entries
        IntegerList start_cell = new IntegerList(new int[num_cells + 1]);        
        for(int i = 0; i<num_cells+1; i++)
            start_cell.set(i, entries[i].log_dim == 0 ? 0 : 4);                    
        start_cell.shiftRight(1);
        int num_new_cells = start_cell.prefixSum(0, num_cells + 1);
                    
        update_entries(start_cell, entries, num_cells);   
                        
        // Partition the set of cells into the sets of those which will be split and those which won't
        IntegerList tmp_ref_ids  = new IntegerList(new int[num_refs * 2]);
        IntegerList tmp_cell_ids = tmp_ref_ids.getSubListFrom(num_refs); 
        int num_sel_refs  = ref_ids.partition_stable(num_refs, tmp_ref_ids, kept_flags); 
        int num_sel_cells = cell_ids.partition_stable(num_refs, tmp_cell_ids, kept_flags);
         
        if(num_sel_refs != num_sel_cells)
            throw new UnsupportedOperationException("num_sel_refs is not equal to num_sel_cells");
        
        //Swap
        /*
            Here ref_ids and cell_ids share same array, hence doing double swap below 
            might corrupt data when arbitrary densities are used.
        
            This is observed in the build_iter loop whereby jumbled ids occur.

            Therefore, only swap once in ref_ids to eliminate the error.
        */
        tmp_ref_ids.swap(ref_ids);
        //tmp_cell_ids.swap(cell_ids); //this is no called as per explanation.
        
        int num_kept = num_sel_refs;
        levels.back().ref_ids  = ref_ids;
        levels.back().cell_ids = cell_ids;
        levels.back().num_kept = num_kept;
        
        if (num_new_cells == 0) {            
            // Exit here because no new reference will be emitted            
            return false;
        }
        
        int num_split = num_refs - num_kept;
                
        // Split the references
        IntegerList split_masks = new IntegerList(new int[num_split + 1]);
        IntegerList start_split = new IntegerList(new int[num_split + 1]);
        
        /// Compute a mask for each reference which determines which sub-cell is intersected
        compute_split_masks(
                cell_ids.getSubListFrom(num_kept), 
                ref_ids.getSubListFrom(num_kept), 
                prims, cells, 
                split_masks, 
                num_split);
                        
        // Store the sub-cells starting index in the entries     
        split_masks.transform(0, num_split + 1, start_split, i -> __popc(i));    
        start_split.shiftRight(1);        
        int num_new_refs = start_split.prefixSum(0, num_split + 1);         
      
        if(!(num_new_refs <= 4 * num_split))        
            throw new UnsupportedOperationException("num_new_refs: " +num_new_refs+ " should be <= " +4*num_split);
                
        IntegerList new_ref_ids = new IntegerList(new int[num_new_refs * 2]);
        IntegerList new_cell_ids = new_ref_ids.getSubListFrom(num_new_refs);
                    
        split_refs(
                cell_ids.getSubListFrom(num_kept), 
                ref_ids.getSubListFrom(num_kept), 
                entries, 
                split_masks, 
                start_split, 
                new_cell_ids, 
                new_ref_ids, 
                num_split);
        
              
        // Emission of the new cells
        Cell[] new_cells   = new Cell[num_new_cells + 0];
        Entry[] new_entries = new Entry[num_new_cells + 1];
        emit_new_cells(entries, cells, new_cells, num_cells);
                
        for(int i = 0; i<num_new_cells + 1; i++)
            new_entries[i] = new Entry();
                
        Level level = new Level();
        level.ref_ids   = new_ref_ids;         
        level.cell_ids  = new_cell_ids;        
        level.num_refs  = num_new_refs;        
        level.num_kept  = num_new_refs;        
        level.cells     = new_cells;           
        level.entries   = new_entries;         
        level.num_cells = num_new_cells;     
        
        levels.add(level);
        
        return true;
    }
    
    public void concat_levels(ObjectList<Level> levels, Grid grid) {
        int num_levels = levels.size();
        
        // Start with references
        int total_refs = 0;
        int total_cells = 0;
        for (Level level : levels) {
            total_refs  += level.num_kept;
            total_cells += level.num_cells;
        }
        
        // Copy primitive references as-is
        IntegerList ref_ids  = new IntegerList(new int[total_refs]);
        IntegerList cell_ids = new IntegerList(new int[total_refs]);
        for (int i = 0, off = 0; i < num_levels; off += levels.get(i).num_kept, i++) {   
            levels.get(i).ref_ids.copyTo(levels.get(i).num_kept, ref_ids.getSubListFrom(off));
            //System.arraycopy(levels.get(i).ref_ids.array(), 0, ref_ids.array(), off, levels.get(i).num_kept);
        }
        
        // Copy the cell indices with an offset       
        for (int i = 0, off = 0, cell_off = 0; i < num_levels; off += levels.get(i).num_kept, cell_off += levels.get(i).num_cells, i++) {
            int num_kept = levels.get(i).num_kept;
            if (num_kept != 0) {
                copy_refs(levels.get(i).cell_ids, cell_ids.getSubListFrom(off), cell_off, num_kept);
                //DEBUG_SYNC();
            }
            levels.get(i).ref_ids = null;
        }
        
        
        
        // Mark the cells at the leaves of the structure as kept
        IntegerList kept_cells = new IntegerList(new int[total_cells + 1]);
        
        for (int i = 0, cell_off = 0; i < num_levels; cell_off += levels.get(i).num_cells, i++) {
            int num_cells = levels.get(i).num_cells;
            mark_kept_cells(levels.get(i).entries, kept_cells.getSubListFrom(cell_off), num_cells);
            //DEBUG_SYNC();
        }
        
        // Compute the insertion position of each cell
        IntegerList start_cell = new IntegerList(new int[total_cells + 1]);  
        kept_cells.copyTo(start_cell).shiftRight(1);
        int new_total_cells = start_cell.prefixSum();
                
        // Allocate new cells, and copy only the cells that are kept
        Cell[] cells = new Cell[new_total_cells];
        for (int i = 0, cell_off = 0; i < num_levels; cell_off += levels.get(i).num_cells, i++) {
            int num_cells = levels.get(i).num_cells;
            copy_cells(levels.get(i).cells, start_cell, cells, cell_off, num_cells);
            //DEBUG_SYNC();
            //mem.free(levels[i].cells);
            levels.get(i).cells = null;
        }
        
        Entry[] entries = new Entry[total_cells];
        for (int i = 0, off = 0; i < num_levels; off += levels.get(i).num_cells, i++) {
            int num_cells = levels.get(i).num_cells;
            int next_level_off = off + num_cells;
            //copy_entries(levels.get(i).entries, start_cell, entries + off, off, next_level_off, num_cells);
            copy_entries(levels.get(i).entries, start_cell, entries, off, next_level_off, num_cells);
            //DEBUG_SYNC();
            //mem.free(levels[i].entries);
            levels.get(i).entries = null;
        }
        
        // Remap the cell indices in the references (which currently map to incorrect cells)
        remap_refs(cell_ids, start_cell, total_refs);
        //DEBUG_SYNC();
        
        // Sort the references by cell (re-use old slots whenever possible)
        IntegerList tmp_ref_ids  = new IntegerList(new int[total_refs]);
        IntegerList tmp_cell_ids = new IntegerList(new int[total_refs]);
        IntegerList new_ref_ids = tmp_ref_ids;
        IntegerList new_cell_ids = tmp_cell_ids;        
        IntegerList.sort_pairs(cell_ids, ref_ids, new_cell_ids, new_ref_ids);
                
        if (!ref_ids.equals(new_ref_ids)) 
            ref_ids.swap(tmp_ref_ids);           
        if (!cell_ids.equals(new_cell_ids)) 
            cell_ids.swap(tmp_cell_ids);  
        
        // Compute the ranges of references for each cell
        compute_cell_ranges(cell_ids, cells, total_refs);
        //DEBUG_SYNC();
        
        grid.entries = entries;
        grid.ref_ids = ref_ids;
        grid.cells   = new ObjectList(cells);
        grid.shift   = levels.size() - 1;
        grid.num_cells   = new_total_cells;
        grid.num_entries = total_cells;
        grid.num_refs    = total_refs;
        
        grid.offsets = new IntegerList(new int[levels.size()]);
        for (int i = 0, off = 0; i < levels.size(); i++) {
            off += levels.get(i).num_cells;
            grid.offsets.set(i, off);
        }        
    }
    
    public void build_grid(Tri[] prims, int num_prims)
    {
        // Allocate a bounding box for each primitive + one for the global bounding box       
        ObjectList<BBox> bboxes = new ObjectList(num_prims + 1, () -> new BBox());
       
        compute_bboxes(prims, bboxes, num_prims);                
        BBox grid_bb = bboxes.reduce(0, num_prims, new BBox(),  (a, b) ->a.copy().extend(b));
        
       
        Vec2i dims = compute_grid_dims(grid_bb, num_prims, hagrid.top_density);
        // Round to the next multiple of 2 on each dimension (in order to align the memory)
        dims.x = (dims.x % 2) != 0 ? dims.x + 1 : dims.x;
        dims.y = (dims.y % 2) != 0 ? dims.y + 1 : dims.y;
        
        
                        
        // Slightly enlarge the bounding box of the grid
        Vec2f extents = grid_bb.extents();
        grid_bb.min = grid_bb.min.sub(extents.mul(0.001f));
        grid_bb.max = grid_bb.max.add(extents.mul(0.001f));
        
        this.hagrid.grid_bbox = grid_bb;
        this.hagrid.grid_dims = dims;
        
        IntegerList log_dims = new IntegerList();
        int[] gridd_shift = new int[1];
        ObjectList<Level> levels = new ObjectList();
        
        // Build top level
        first_build_iter(hagrid.snd_density, prims, num_prims, bboxes, grid_bb, dims, log_dims, gridd_shift, levels);
                
        int iter = 1; //build iterations
        while(this.build_iter(prims, num_prims, dims, log_dims, levels, iter))          
            iter++;
          
        System.out.println("iteration " +iter);
        
        concat_levels(levels, hagrid.getIrregularGrid());
                   
        hagrid.getIrregularGrid().bbox = grid_bb;
        hagrid.getIrregularGrid().dims = dims;
        
        //engine.setMCellInfo(MCellInfo.getCells(engine, hagrid.getIrregularGrid(), hagrid.getIrregularGrid().bbox, hagrid.getIrregularGrid().dims, hagrid.getIrregularGrid().shift));
        
    }

}
