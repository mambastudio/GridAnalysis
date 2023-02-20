/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import gridanalysis.algorithm.Build;
import gridanalysis.algorithm.Expand;
import gridanalysis.algorithm.Merge;
import gridanalysis.coordinates.Vec2f;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import gridanalysis.irreg.Float2;
import gridanalysis.irreg.Tri2;
import gridanalysis.jfx.math.MTransform;
import gridanalysis.jfx.shape.MCellInfo;
import gridanalysis.jfx.shape.MRectangle;
import gridanalysis.jfx.shape.MTriangle;
import gridanalysis.utilities.Utility;
import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author user
 */
public class MEngine {
    MTransform transform = MTransform.translate(100, 100);
    GraphicsContext ctx;   
    
    ArrayList<MTriangle> mtriangles;
    ArrayList<Tri> triangles;
    
    ArrayList<MCellInfo> cellInfo = new ArrayList();
    
    Grid grid = new Grid();
    float top_density = 0.12f;
    float snd_density = 1.4f;
    float alpha = 0.995f;
    int exp_iters = 3;
    
    public void draw()
    {
        ctx.save();
        transform.transformGraphicsContext(ctx);
        mtriangles.forEach(mtri -> {
            mtri.draw();
        });
        
        new MRectangle(ctx, grid.bbox).draw();        
        drawMCellInfo();
        
        ctx.restore();
    }
    
    public void setGraphicsContext(GraphicsContext context)
    {
        /*
        this.ctx = context;
        this.triangles = new ArrayList();
        mtriangles = Utility.generateTriangles(ctx, triangles, 2, new Vec2f(0, 0), new Vec2f(500, 500));
        
        Tri[] tris = new Tri[triangles.size()];
        triangles.toArray(tris);
        
        Build build = new Build(this);
        build.build_grid((Tri[]) tris, triangles.size(), grid, top_density, snd_density);
        
        Merge merge = new Merge(this);
        merge.merge_grid(grid, alpha);
        
        //Expand expand = new Expand(this);
        //expand.expand_grid(grid, tris, exp_iters);
        */
        
        this.ctx = context;
        
        Tri2 tris[] = new Tri2[2];
        tris[0] = new Tri2(new Float2(370.77f, 330.81f), new Float2(316.49f, 137.53f), new Float2(392.41f, 180.43f));
        tris[1] = new Tri2(new Float2(74.20f, 85.51f), new Float2(77.92f, 321.43f), new Float2(218.57f, 6.09f));
        
        mtriangles = new ArrayList();
        mtriangles.add(new MTriangle(ctx, tris[0]));
        mtriangles.add(new MTriangle(ctx, tris[1]));
    }    
    
    public void drawMCellInfo()
    {
        for(MCellInfo info: cellInfo)
            info.draw();
    }
    
    public GraphicsContext getGraphicsContext()
    {
        return ctx;
    }
        
    public void setMCellInfo(ArrayList<MCellInfo>... cellInfoArray)
    {
        this.cellInfo.clear();
        for(ArrayList<MCellInfo> cellInfoList : cellInfoArray)
            this.cellInfo.addAll(cellInfoList);
    }
}
