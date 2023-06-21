/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import gridanalysis.algorithm.EngineAbstract;
import gridanalysis.algorithm.HagridConstruction;
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
public class MEngine implements EngineAbstract{
    MTransform transform = MTransform.translate(100, 100);
    GraphicsContext ctx;   
    
    ArrayList<MTriangle> mtriangles;
    ArrayList<Tri> triangles;
    
    ArrayList<MCellInfo> cellInfo = new ArrayList();
    Grid grid;
    
    MouseActivity mouseActivity;
    //Hagrid hagrid = new Hagrid();
    
    @Override
    public void draw()
    {
        ctx.clearRect(0, 0, 5000, 5000);
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
    public void setGraphicsContext(GraphicsContext context)
    {        
        this.ctx = context;
        this.triangles = new ArrayList();
        mtriangles = Utility.generateTriangles(ctx, triangles, 2, new Vec2f(0, 0), new Vec2f(500, 500));
        
        Tri[] tris = new Tri[triangles.size()];
        triangles.toArray(tris);
       
        HagridConstruction hagridConstruction = new HagridConstruction(this);
        grid = hagridConstruction.initialiseGrid(tris);      
        
    }    
    
    @Override
    public void drawMCellInfo()
    {
        cellInfo.forEach(info -> {
            info.draw();
        });
    }
    
    @Override
    public GraphicsContext getGraphicsContext()
    {
        return ctx;
    }
        
    @Override
    public void setMCellInfo(ArrayList<MCellInfo>... cellInfoArray)
    {
        this.cellInfo.clear();
        for(ArrayList<MCellInfo> cellInfoList : cellInfoArray)
            this.cellInfo.addAll(cellInfoList);
    }

    @Override
    public void setMouseActivity(MouseActivity mouseActivity) {
        this.mouseActivity = mouseActivity;
    }
}
