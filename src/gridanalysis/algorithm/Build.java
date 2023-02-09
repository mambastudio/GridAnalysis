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
import gridanalysis.utilities.IntArray;
import gridanalysis.utilities.Utility;
import static java.lang.Math.cbrt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author user
 */
public class Build extends GridAbstracts{
    MEngine engine;
    
    Vec2i grid_dims;
    BBox  grid_bbox;
    Vec2f cell_size;
    int   grid_shift;
    
    public Build(MEngine engine)
    {
        this.engine = engine;
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
                (int)(extents.x * ratio),
                (int)(extents.y * ratio)));
    }
    
    public void compute_bboxes(
            Tri[] prims,
            BBox[] bboxes,
            int num_prims) 
    {
        for(int i = 0; i<num_prims; i++)
            bboxes[i] = prims[i].bbox();
    }
    
    /// Compute an over-approximation of the number of references
    /// that are going to be generated during reference emission
    public void count_new_refs(
            BBox[]  bboxes,
            IntArray counts,
            int num_prims) {
        
        for(int id = 0; id<num_prims; id++)
        {
            if (id >= num_prims) return;
            
            BBox ref_bb = bboxes[id];//load_bbox(bboxes + id);
            Range range  = compute_range(grid_dims, grid_bbox, ref_bb);
            counts.set(id, Math.max(0, range.size()));               
        }    
    }
    
    /// Given a position on the virtual grid, return the corresponding top-level cell index
    public int top_level_cell(Vec2i pos) {
        return (pos.x >> grid_shift) + grid_dims.x * ((pos.y >> grid_shift));
    }
    
    /// Returns a voxel map entry with the given dimension and starting index
    public Entry make_entry(int log_dim, int begin) {
        Entry e = new Entry(log_dim, begin);
        return e;
    }
    
    /// Update the logarithm of the sub-level resolution for top-level cells (after a new subdivision level)
    public void update_log_dims(IntArray log_dims, int num_top_cells) {
        for(int id = 0; id<num_top_cells; id++)
        {
            if (id >= num_top_cells) return;            
            log_dims.set(id, max(0, log_dims.get(id) - 1));            
        }
    }
    
    /// Mark references that are kept so that they can be moved to the beginning of the array
    public void mark_kept_refs(
            IntArray cell_ids,
            Entry[] entries,
            IntArray kept_flags,
            int num_refs) {
        for(int id = 0; id<num_refs; id++)
        {            
            if (id >= num_refs) return;

            int cell_id = cell_ids.get(id);  
            
            int value = ((cell_id >= 0) && (entries[cell_id].log_dim == 0)) ? 1 : 0;              
            kept_flags.set(id, value);
        }             
    }
    
    public void count_refs_per_cell(IntArray cell_ids,
                                    IntArray refs_per_cell,                                    
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
            IntArray refs_per_cell,
            IntArray log_dims,
            float snd_density,
            int num_cells) {
        
        for(int id = 0; id<num_cells; id++)
        {         
            if (id >= num_cells) return;

            Vec2f extents = grid_bbox.extents().div(new Vec2f(grid_dims));
            BBox bbox = new BBox(new Vec2f(), extents);
            
            Vec2i dims = compute_grid_dims(bbox, refs_per_cell.get(id), snd_density);
            int max_dim = max(dims.x, dims.y);
            int log_dim = 31 - Integer.numberOfLeadingZeros(max_dim);
            log_dim = (1 << log_dim) < max_dim ? log_dim + 1 : log_dim;
            log_dims.set(id, log_dim);
        }
    }
    
    /// Update the entries for the one level before the current one
    public void update_entries(
            IntArray start_cell,
            Entry[] entries,
            int num_cells) {
        for(int id = 0; id<num_cells; id++)
        {            
            if (id >= num_cells) return;

            int start = start_cell.get(id);
            Entry entry = entries[id];

            // If the cell is subdivided, write the first sub-cell index into the current entry
            entry.begin = entry.log_dim != 0 ? start : id;
            entries[id] = entry;
        }
    }
       
    public void emit_new_refs(
            BBox[] bboxes,
            IntArray start_emit,
            IntArray new_ref_ids,
            IntArray new_cell_ids,
            int num_prims){
        for(int id = 0; id<num_prims; id++)
        {
            int start = start_emit.get(id + 0);
            int end   = start_emit.get(id + 1);
            
            if (start < end) 
            {
                BBox ref_bb = bboxes[id];
                Range range  = compute_range(grid_dims, grid_bbox, ref_bb);
            
                int x = range.lx;
                int y = range.ly;               
                int cur = start;
                while (cur < end) 
                {
                    new_ref_ids .set(cur, id);
                    new_cell_ids.set(cur, x + grid_dims.x * (y));
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

            int x = id % grid_dims.x;
            int y = (id / grid_dims.x) % grid_dims.y;
            int inc = 1 << grid_shift;

            x <<= grid_shift;
            y <<= grid_shift;           

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
            Cell cell = cells[id];
            int min_x = cell.min.x;
            int min_y = cell.min.y;
            int inc = (cell.max.x - cell.min.x) >> 1;

            for (int i = 0; i < 4; i++) {
                int x = min_x + (i % 2) * inc;
                int y = min_y + ((i >> 1)) * inc;

                cell.min = new Vec2i(x, y);
                cell.max = new Vec2i(x + inc, y + inc);
                cell.begin = 0;
                cell.end   = 0;
                new_cells[start + i] = cell; 
            }
        }
    }
    
    /// Count the (sub-)dimensions of each cell, based on the array of references
    public void compute_dims(
            IntArray  cell_ids,
            Cell[] cells,
            IntArray log_dims,
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
    
    public void compute_split_masks(IntArray cell_ids,
                                    IntArray ref_ids,
                                    Tri[] prims,
                                    Cell[] cells,
                                    IntArray split_masks,
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
            Cell cell = cells[cell_id];              
            Tri prim = prims[ref];

            Vec2f cell_min = grid_bbox.min.add(cell_size.mul(new Vec2f(cell.min)));
            Vec2f cell_max = grid_bbox.min.add(cell_size.mul(new Vec2f(cell.max)));
            Vec2f middle = (cell_min.add(cell_max)).mul(0.5f);

            int mask = 0xF;

            // Optimization: Test against half spaces first
            BBox ref_bb = prim.bbox();
            if (ref_bb.min.x > cell_max.x ||
                ref_bb.max.x < cell_min.x) mask  = 0;
            if (ref_bb.min.x >   middle.x) mask &= 0xAA;
            if (ref_bb.max.x <   middle.x) mask &= 0x55;
            if (ref_bb.min.y > cell_max.y ||
                ref_bb.max.y < cell_min.y) mask  = 0;
            if (ref_bb.min.y >   middle.y) mask &= 0xCC;
            if (ref_bb.max.y <   middle.y) mask &= 0x33;            

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
                    IntArray cell_ids,
                    IntArray ref_ids,
                    Entry[] entries,
                    IntArray split_masks,
                    IntArray start_split,
                    IntArray new_cell_ids,
                    IntArray new_ref_ids,
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
            IntArray cell_ids,
            IntArray ref_ids,
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
                    grid_bbox.min.add(new Vec2f(cell.min).mul(cell_size)),
                    grid_bbox.min.add(new Vec2f(cell.max).mul(cell_size)));     
            
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
            BBox[] bboxes, BBox grid_bb, Vec2i dims,
            IntArray log_dims, int[] grid_shift, ArrayList<Level> levels) 
    {
        int num_top_cells = dims.x * dims.y;
       
        IntArray start_emit        = new IntArray(new int[num_prims + 1]);
        IntArray new_ref_counts    = new IntArray(new int[num_prims + 1]);
        IntArray refs_per_cell     = new IntArray(new int[num_top_cells]);
        //int[] log_dims          = new int[num_top_cells + 1];
        
        count_new_refs(bboxes, new_ref_counts, num_prims);  
        int num_new_refs = IntArray.exclusiveScan(new_ref_counts, num_prims + 1, start_emit);
        
        IntArray new_ref_ids  = new IntArray(new int[2 * num_new_refs]);
        IntArray new_cell_ids = new_ref_ids.splitSubArrayFrom(num_new_refs);   
        emit_new_refs(bboxes, start_emit, new_ref_ids, new_cell_ids, num_prims); 
                
        // Compute the number of references per cell
        count_refs_per_cell(new_cell_ids, refs_per_cell, num_new_refs);
        
        // Compute an independent resolution in each of the top-level cells
        compute_log_dims(refs_per_cell, log_dims, snd_density, num_top_cells);
                
        // Find the max sub-level resolution
        grid_shift[0] = Arrays.stream(log_dims.array(), 0, num_top_cells).reduce(0, (a, b) -> Math.max(a, b));
        
        this.cell_size = grid_bb.extents().div(new Vec2f(dims.leftShift(grid_shift[0])));
        this.grid_shift = grid_shift[0];
        
        //Emission of the new cells
        Cell[] new_cells   = new Cell[num_top_cells + 0];
        Entry[] new_entries = new Entry[num_top_cells + 1];
        
        emit_top_cells(new_cells, num_top_cells);
        for(int i = 0; i<num_top_cells + 1; i++)
            new_entries[i] = new Entry();
        
        
        
        // Filter out the references that do not intersect the cell they are in
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
                Vec2i dims, IntArray log_dims,
                ArrayList<Level> levels)
    {
        IntArray cell_ids  = levels.get(levels.size()-1).cell_ids;
        IntArray ref_ids   = levels.get(levels.size()-1).ref_ids;
        Cell[] cells    = levels.get(levels.size()-1).cells;
        Entry[] entries = levels.get(levels.size()-1).entries;
        
        int num_top_cells = dims.x * dims.y;
        int num_refs  = levels.get(levels.size()-1).num_refs;
        int num_cells = levels.get(levels.size()-1).num_cells;

        int cur_level  = levels.size();
        
        IntArray kept_flags = new IntArray(new int[num_refs + 1]);
        
        // Find out which cell will be split based on whether it is empty or not and the max depth
        compute_dims(cell_ids, cells, log_dims, entries, num_refs);         
        update_log_dims(log_dims, num_top_cells);        
        mark_kept_refs(cell_ids, entries, kept_flags, num_refs);
                        
        // Store the sub-cells starting index in the entries
        IntArray start_cell = new IntArray(new int[num_cells + 1]);
        for(int i = 0; i<num_cells; i++)
            start_cell.set(i, entries[i].log_dim == 0 ? 0 : 4);        
        int num_new_cells = IntArray.exclusiveScan(start_cell, num_cells + 1, start_cell);

        update_entries(start_cell, entries, num_cells);   
        
        //engine.setMCellInfo(MCellInfo.getCells(engine, cell_ids, kept_flags, this.grid_bbox, dims));
        
        // Partition the set of cells into the sets of those which will be split and those which won't
        IntArray tmp_ref_ids  = new IntArray(new int[num_refs * 2]);
        IntArray tmp_cell_ids = tmp_ref_ids.getSubArray(num_refs, tmp_ref_ids.size());
        int num_sel_refs  = IntArray.partition(ref_ids,  tmp_ref_ids,  num_refs, kept_flags);
        int num_sel_cells = IntArray.partition(cell_ids, tmp_cell_ids, num_refs, kept_flags);
                
        if(num_sel_refs != num_sel_cells)
            throw new UnsupportedOperationException("num_sel_refs is not equal to num_sel_cells");
        
        //Swap
        tmp_ref_ids.swap(ref_ids);
        tmp_cell_ids.swap(cell_ids);
                
        int num_kept = num_sel_refs;
        levels.get(levels.size()-1).ref_ids  = ref_ids;
        levels.get(levels.size()-1).cell_ids = cell_ids;
        levels.get(levels.size()-1).num_kept = num_kept;
        
        if (num_new_cells == 0) {
            // Exit here because no new reference will be emitted            
            return false;
        }
        int num_split = num_refs - num_kept;
                
        // Split the references
        IntArray split_masks = new IntArray(new int[num_split + 1]);
        IntArray start_split = new IntArray(new int[num_split + 1]);
        
        compute_split_masks(
                cell_ids.splitSubArrayFrom(num_kept), 
                ref_ids.splitSubArrayFrom(num_kept), 
                prims, cells, 
                split_masks, 
                num_split);
                
        // Store the sub-cells starting index in the entries        
        for(int i = 0; i<split_masks.size(); i++)
            start_split.set(i, __popc(split_masks.get(i)));
        int num_new_refs = IntArray.exclusiveScan(start_split, num_split+ + 1, start_split);
                
        if(!(num_new_refs <= 4 * num_split))
        {
            
            throw new UnsupportedOperationException("num_new_refs: " +num_new_refs+ " should be <= " +4*num_split);
        }
        
        IntArray new_ref_ids = new IntArray(new int[num_new_refs * 2]);
        IntArray new_cell_ids = new_ref_ids.splitSubArrayFrom(num_new_refs);
               
        split_refs(
                cell_ids.splitSubArrayFrom(num_kept), 
                ref_ids.splitSubArrayFrom(num_kept), 
                entries, 
                split_masks, 
                start_split, 
                new_cell_ids, 
                new_ref_ids, 
                num_split);
        //engine.setMCellInfo(MCellInfo.getCells(engine, cells, entries, this.grid_bbox, this.grid_dims, this.grid_shift));
        // Emission of the new cells
        Cell[] new_cells   = new Cell[num_new_cells + 0];
        Entry[] new_entries = new Entry[num_new_cells + 1];
        emit_new_cells(entries, cells, new_cells, num_cells);
        
        for(int i = 0; i<num_new_cells + 1; i++)
            new_entries[i] = new Entry();
        
        System.out.println(num_new_cells);
        engine.setMCellInfo(MCellInfo.getCells(engine, new_cells, this.grid_bbox, this.grid_dims, this.grid_shift));
        Level level = new Level();
        level.ref_ids   = new_ref_ids;         
        level.cell_ids  = new_cell_ids;        
        level.num_refs  = num_new_refs;        
        level.num_kept  = num_new_refs;        
        level.cells     = new_cells;           
        level.entries   = new_entries;         
        level.num_cells = num_new_cells;     
        
        levels.add(level);
                
        return false;
    }
    
    public void build_grid(Tri[] prims, int num_prims, Grid grid, float top_density, float snd_density)
    {
        // Allocate a bounding box for each primitive + one for the global bounding box
        BBox[] bboxes = new BBox[num_prims + 1];
       
        compute_bboxes(prims, bboxes, num_prims);
       
        BBox grid_bb = Arrays.stream(bboxes, 0, num_prims).reduce(new BBox(), (a, b) ->{
            a.extend(b); return a;});
        
        Vec2i dims = compute_grid_dims(grid_bb, num_prims, top_density);
        // Round to the next multiple of 2 on each dimension (in order to align the memory)
        dims.x = (dims.x % 2) != 0 ? dims.x + 1 : dims.x;
        dims.y = (dims.y % 2) != 0 ? dims.y + 1 : dims.y;
        
        // Slightly enlarge the bounding box of the grid
        Vec2f extents = grid_bb.extents();
        grid_bb.min = grid_bb.min.sub(extents.mul(0.001f));
        grid_bb.max = grid_bb.max.add(extents.mul(0.001f));
        
        this.grid_bbox = grid_bb;
        this.grid_dims = dims;
        
        IntArray log_dims = new IntArray(new int[dims.x * dims.y + 1]);
        int[] grid_shift = new int[1];
        ArrayList<Level> levels = new ArrayList();

        // Build top level
        first_build_iter(snd_density, prims, num_prims, bboxes, grid_bb, dims, log_dims, grid_shift, levels);
        
        int iter = 1;
        while(this.build_iter(prims, num_prims, dims, log_dims, levels))
            iter++;
        
        ArrayList<Cell> cellList = new ArrayList();
        for(Level level : levels)
        {
            cellList.addAll(Arrays.asList(level.cells));
        }
        
        Cell[] cells = new Cell[cellList.size()];
        cellList.toArray(cells);
        
        //System.out.println("cell count " +cells.length);
        
        //engine.setMCellInfo(MCellInfo.getCells(engine, cells, this.grid_bbox, dims, this.grid_shift));
        
        //System.out.println(levels.size());
                
        grid.bbox = grid_bb;
        grid.dims = dims;
    }

}
