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
    
    Vec2i grid_dims;
    BBox  grid_bbox;
    Vec2f cell_size;
    int   grid_shift;
    
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
            Range range = null;

            if (start < end) 
            {
                BBox ref_bb = bboxes[id];
                range  = compute_range(grid_dims, grid_bbox, ref_bb);
            }

            if (start < end) 
            {
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
                
        System.out.println(num_top_cells);
        System.out.println(num_new_refs);
        
        System.out.println(new_cell_ids);        
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
