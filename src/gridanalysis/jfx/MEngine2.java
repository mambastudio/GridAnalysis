/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import gridanalysis.algorithm.EngineAbstract;
import gridanalysis.irreg.BBox2;
import gridanalysis.irreg.Cell2;
import gridanalysis.irreg.Float2;
import static gridanalysis.irreg.Float2.sub;
import gridanalysis.irreg.Grid2;
import gridanalysis.irreg.GridInfo;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.irreg.Merge2;
import gridanalysis.irreg.Optimise_Overlap;
import gridanalysis.irreg.Ref;
import gridanalysis.irreg.Tri2;
import static gridanalysis.irreg.Voxel_Map.lookup_entry;
import gridanalysis.jfx.math.MTransform;
import gridanalysis.jfx.shape.MCellInfo;
import gridanalysis.jfx.shape.MRectangle;
import gridanalysis.jfx.shape.MTriangle;
import gridanalysis.utilities.list.ObjectList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author jmburu
 */
public class MEngine2 implements EngineAbstract{
    
    MTransform transform = MTransform.translate(100, 100);
    GraphicsContext ctx;   
    
    ObjectList<MTriangle> mtriangles;
    ObjectList<Tri2> triangles;
    
    ObjectList<MCellInfo> cellInfoList = new ObjectList();
    
    GridInfo grid = new GridInfo();
    float top_density = 0.12f;
    float snd_density = 1.4f;
    float alpha = 0.995f;
    
    MouseActivity mouseActivity;
    
    int exp_iters = 3;

    @Override
    public void draw() {
        ctx.save();
        transform.transformGraphicsContext(ctx);
        mtriangles.forEach(mtri -> {
            mtri.draw();
        });
        
        new MRectangle(ctx, grid.bbox).draw();        
        drawMCellInfo();
        
        ctx.restore();
    }

    @Override
    public void drawMCellInfo() {
        cellInfoList.forEach(info -> {
            info.draw();
        });
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void setGraphicsContext(GraphicsContext context) {
        this.ctx = context;
        
        float x = 0;
        Tri2 tris[] = new Tri2[2];
        //tris[0] = new Tri2(new Float2(370.77f + x, 330.81f), new Float2(316.49f +x, 137.53f), new Float2(392.41f + x, 180.43f));
        //tris[1] = new Tri2(new Float2(74.20f, 85.51f), new Float2(77.92f, 321.43f), new Float2(218.57f, 6.09f));
        
        tris[0] = new Tri2(new Float2(401.03f, 170.69f), new Float2(430.96f, 10.98f), new Float2(5.63f, 33.08f));
        tris[1] = new Tri2(new Float2(417.81f, 299.59f), new Float2(385.65f, 185.33f), new Float2(488.00f, 468.06f));
        
        mtriangles = new ObjectList();
        mtriangles.add(new MTriangle(ctx, tris[0]));
        mtriangles.add(new MTriangle(ctx, tris[1]));
        
        BBox2[] bboxes = new BBox2[tris.length];
        ObjectList<Ref> refs = new ObjectList();
        ObjectList<Cell2> cells = new ObjectList();
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
        
        call(info, refs, cells, snd_dims, entries);
        
        boolean do_overlap = false;
        if (do_overlap) {
            boolean[] cell_flags = new boolean[cells.size()];
            Arrays.fill(cell_flags, true);     
            
            while (Optimise_Overlap.optimize_overlap(info, entries, ref_ids, tris, cell_flags, cells) > 3 * (1 - alpha) * cells.size()) ;           
        }
        
        Grid2.transform_cells(info, cells);
        
      
        IntStream.range(0, cells.size())
                .forEach(i->{
                    Cell2 cell = cells.get(i);
                    
                    MCellInfo cellInfo = new MCellInfo(ctx, cell.getBound());
                    cellInfo.object = i;
                    cellInfoList.add(cellInfo);                   
                });
        
       
        
        
        
        this.grid = info;
    }
    
    public void call(
                GridInfo info,
                ObjectList<Ref> refs,  
                ObjectList<Cell2> cells,
                IntegerList snd_dims, 
                IntegerList entries   
    )
    {
        int cell_id = 12;
        Cell2 cell = cells.get(cell_id);
        
        int axis = 0;
        
        int[] xy = new int[2];
        xy[0] = cell.max[axis];
        xy[1] = cell.min[axis + 1] + 3;
        
        int entry = lookup_entry(entries, info.dims, info.max_snd_dim, xy[0], xy[1]);
        System.out.println(entry);
    }
    

    @Override
    public GraphicsContext getGraphicsContext() {
        return ctx;
    }

    @Override
    public void setMCellInfo(ArrayList<MCellInfo>... cellInfoArray) {
        this.cellInfoList.clear();
        for(ArrayList<MCellInfo> cellInfoArrayList : cellInfoArray)
            this.cellInfoList.addAll(cellInfoArrayList);
    }

    @Override
    public void setMouseActivity(MouseActivity mouseActivity) {
        this.mouseActivity = mouseActivity;
    }

    @Override
    public void test() {
        System.out.println("no test");
    }
    
}
