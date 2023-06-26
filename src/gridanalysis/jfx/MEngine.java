/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import gridanalysis.algorithm.EngineAbstract;
import gridanalysis.algorithm.GridAbstracts;
import gridanalysis.algorithm.HagridConstruction;
import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.Cell;
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
        mtriangles = Utility.generateTriangles(ctx, triangles, 2, new Vec2f(0, 0), new Vec2f(600, 600));
        
        Tri[] tris = new Tri[triangles.size()];
        triangles.toArray(tris);
       
        HagridConstruction hagridConstruction = new HagridConstruction(this);
        grid = hagridConstruction.initialiseGrid(tris);      
        System.out.println("grid size: " +grid.bbox);
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

    @Override
    public void test() {
        Vec2f mousePoint = new Vec2f(
                mouseActivity.getXFloatPoint(transform.inverseTransform()), 
                mouseActivity.getYFloatPoint(transform.inverseTransform()));
        
        Vec2i   grid_dims   = grid.dims.leftShift(grid.shift);
        int     grid_shift  = grid.shift;
        
        if(grid.bbox.is_inside(mousePoint))
        {
            Vec2f comp_voxel = compute_voxel(mousePoint);    
            Vec2i voxel = Vec2i.clamp(new Vec2i(comp_voxel), new Vec2i(), grid_dims.sub(1));
            int entry = GridAbstracts.lookup_entry(grid.entries, grid_shift, grid_dims.rightShift(grid_shift), voxel);
            Cell cell = grid.cells.get(entry);
            
            System.out.println(entry);
            System.out.println("cell has reference: " +cell.hasReference());
            //System.out.println(cell.extents());
            
        }
    }
    
    private Vec2f compute_voxel(Vec2f point)
    {
        Vec2f extents = grid.bbox.extents();
        Vec2i dims = grid.dims.leftShift(grid.shift);
        Vec2f grid_inv  = new Vec2f(dims).div(extents);
        
        return point.sub(grid.bbox.min).mul(grid_inv);
    }
}
