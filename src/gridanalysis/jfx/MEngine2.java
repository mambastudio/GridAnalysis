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
import gridanalysis.irreg.IntList;
import gridanalysis.irreg.Ref;
import gridanalysis.irreg.Tri2;
import gridanalysis.jfx.math.MTransform;
import gridanalysis.jfx.shape.MCellInfo;
import gridanalysis.jfx.shape.MRectangle;
import gridanalysis.jfx.shape.MTriangle;
import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author jmburu
 */
public class MEngine2 implements EngineAbstract{
    
    MTransform transform = MTransform.translate(100, 100);
    GraphicsContext ctx;   
    
    ArrayList<MTriangle> mtriangles;
    ArrayList<Tri2> triangles;
    
    ArrayList<MCellInfo> cellInfo = new ArrayList();
    
    GridInfo grid = new GridInfo();
    float top_density = 0.12f;
    float snd_density = 1.4f;
    float alpha = 0.995f;
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
        cellInfo.forEach(info -> {
            info.draw();
        });
    }

    @Override
    public void setGraphicsContext(GraphicsContext context) {
        this.ctx = context;
        
        Tri2 tris[] = new Tri2[2];
        tris[0] = new Tri2(new Float2(370.77f, 330.81f), new Float2(316.49f, 137.53f), new Float2(392.41f, 180.43f));
        tris[1] = new Tri2(new Float2(74.20f, 85.51f), new Float2(77.92f, 321.43f), new Float2(218.57f, 6.09f));
        
        mtriangles = new ArrayList();
        mtriangles.add(new MTriangle(ctx, tris[0]));
        mtriangles.add(new MTriangle(ctx, tris[1]));
        
        BBox2[] bboxes = new BBox2[2];
        ArrayList<Ref> refs = new ArrayList();
        ArrayList<Cell2> cells = new ArrayList();
        IntList snd_dims = new IntList();
        
        GridInfo info = new GridInfo();
        
        Grid2.compute_bboxes(info, tris, bboxes);
        Grid2.compute_grid_dims(sub(info.bbox.max, info.bbox.min), tris.length, top_density, info.dims);
        Grid2.gen_top_refs(info, bboxes, tris, refs);
        
        Grid2.compute_snd_dims(info, snd_density, refs, snd_dims);
        Grid2.subdivide_refs(info, tris, snd_dims, refs);
        
        Grid2.gen_cells(info, refs, snd_dims, cells);
        
        Grid2.transform_cells(info, cells);
        
        this.grid = info;
    }

    @Override
    public GraphicsContext getGraphicsContext() {
        return ctx;
    }

    @Override
    public void setMCellInfo(ArrayList<MCellInfo>... cellInfoArray) {
        this.cellInfo.clear();
        for(ArrayList<MCellInfo> cellInfoList : cellInfoArray)
            this.cellInfo.addAll(cellInfoList);
    }
    
}
