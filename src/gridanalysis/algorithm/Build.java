/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import java.util.Arrays;

/**
 *
 * @author user
 */
public class Build {
    public void compute_bboxes(
            Tri[] prims,
            BBox[] bboxes,
            int num_prims) 
    {
        for(int i = 0; i<num_prims; i++)
            bboxes[i] = prims[i].bbox();
    }
    public void build_grid(Tri[] tris, int num_prims, Grid grid, float top_density, float snd_density)
    {
        // Allocate a bounding box for each primitive + one for the global bounding box
        BBox[] bboxes = new BBox[num_prims + 1];
       
        compute_bboxes(tris, bboxes, num_prims);
       
        BBox grid_bb = Arrays.stream(bboxes, 0, num_prims).reduce(new BBox(), (a, b) ->{
            a.extend(b); return a;});
        
        grid.bbox = grid_bb;
    }
}
