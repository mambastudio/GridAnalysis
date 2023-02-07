/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Level;
import gridanalysis.gridclasses.Range;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.MEngine;
import gridanalysis.jfx.shape.MCellInfo;
import gridanalysis.utilities.IntArray;
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
        
        // Find the maximum sub-level resolution
        grid_shift[0] = Arrays.stream(log_dims.array(), 0, num_top_cells).reduce(0, (a, b) -> Math.max(a, b));
                
        engine.setMCellInfo(MCellInfo.getCells(engine, new_cell_ids, grid_bb, dims));
                
        System.out.println(grid_shift[0]);
        
        System.out.println(log_dims);        
        System.out.println(refs_per_cell);
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
                
        grid.bbox = grid_bb;
        grid.dims = dims;
    }

}
