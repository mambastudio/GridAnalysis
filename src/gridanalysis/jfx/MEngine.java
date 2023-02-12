/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import gridanalysis.algorithm.Build;
import gridanalysis.algorithm.Merge;
import gridanalysis.coordinates.Vec2f;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
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
    float snd_density = 2.4f;
    float alpha = 0.995f;
    
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
        this.ctx = context;
        this.triangles = new ArrayList();
        mtriangles = Utility.generateTriangles(ctx, triangles, 2, new Vec2f(0, 0), new Vec2f(500, 500));
        
        Tri[] tris = new Tri[triangles.size()];
        triangles.toArray(tris);
        
        Build build = new Build(this);
        build.build_grid((Tri[]) tris, triangles.size(), grid, top_density, snd_density);
        
        Merge merge = new Merge(this);
        merge.merge_grid(grid, alpha);
        
        System.out.println("kubafu");
        
        setMCellInfo(MCellInfo.getCells(this, grid.cells, grid.bbox, grid.dims, grid.shift));
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
