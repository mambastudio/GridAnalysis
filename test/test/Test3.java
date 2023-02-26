/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.irreg.BBox2;
import gridanalysis.irreg.Cell2;
import gridanalysis.irreg.Float2;
import static gridanalysis.irreg.Float2.sub;
import gridanalysis.irreg.Grid2;
import gridanalysis.irreg.GridInfo;
import gridanalysis.irreg.IntegerList;
import gridanalysis.irreg.Merge2;
import gridanalysis.irreg.Ref;
import gridanalysis.irreg.Tri2;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author user
 */
public class Test3 {
    public static void main(String... args)
    {
        float top_density = 0.12f;
        float snd_density = 1.05f;
        float alpha = 0.995f;
        
        Tri2 tris[] = new Tri2[2];
        tris[0] = new Tri2(new Float2(370.77f, 330.81f), new Float2(316.49f, 137.53f), new Float2(392.41f, 180.43f));
        tris[1] = new Tri2(new Float2(74.20f, 85.51f), new Float2(77.92f, 321.43f), new Float2(218.57f, 6.09f));
        
        BBox2[] bboxes = new BBox2[2];
        ArrayList<Ref> refs = new ArrayList();
        ArrayList<Cell2> cells = new ArrayList();
        IntegerList snd_dims = new IntegerList();
        IntegerList entries = new IntegerList();
        
        GridInfo info = new GridInfo();
        
        Grid2.compute_bboxes(info, tris, bboxes);
        Grid2.compute_grid_dims(sub(info.bbox.max, info.bbox.min), tris.length, top_density, info.dims);
        Grid2.gen_top_refs(info, bboxes, tris, refs);
        
        Grid2.compute_snd_dims(info, snd_density, refs, snd_dims);
        Grid2.subdivide_refs(info, tris, snd_dims, refs);
        
        Grid2.gen_cells(info, refs, snd_dims, cells);
        Grid2.gen_entries(info, cells, snd_dims, entries);
        
        // Compute the array of references to send to the GPU
        IntegerList ref_ids = new IntegerList();
        ref_ids.resize(refs.size());
        for (int i = 0; i < ref_ids.size(); i++)
            ref_ids.set(i, refs.get(i).tri);
        
        // Optimizations happen in integer virtual grid coordinates
        boolean do_merge = true;
        if (do_merge) {            
            int iter = 0;
            int before, after;
            do {                
                before = cells.size();
                Merge2.merge(iter++, info, cells, ref_ids, entries);
                after = cells.size();
                
                
            } while (after < before * alpha);            
        }
        
        Grid2.transform_cells(info, cells);
                
       // System.out.println(cells);
       // System.out.println(info.cell_size());
       // System.out.println(info.bbox);
    }
}
