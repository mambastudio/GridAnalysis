/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx.shape;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.jfx.MEngine;
import gridanalysis.utilities.IntArray;
import gridanalysis.utilities.Utility;
import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author user
 */
public class MCellInfo {
    private final GraphicsContext ctx;
    private final double x;
    private final double y;
    private final double w;
    private final double h;
    
    public MCellInfo(GraphicsContext context, BBox bbox)
    {
        this.ctx = context;
        this.x = bbox.min.x;
        this.y = bbox.min.y;
        this.w = bbox.extents().x;
        this.h = bbox.extents().y;
    }
    
    public void draw()
    {
        ctx.save();
        
        ctx.strokeRect(x, y, w, h);
        
        ctx.restore();
    }
    
    public static ArrayList<MCellInfo> getCells(MEngine engine, IntArray cell_ids, BBox grid_bound,  Vec2i dims)
    {
        ArrayList<MCellInfo> cells = new ArrayList();
        for(int i = 0; i<cell_ids.size(); i++)
        {
            Vec2f cellExtents = Utility.getBox(dims, grid_bound);
            Vec2i cellCoords = Utility.getGridCoord(cell_ids.get(i), dims);
            
            Vec2f cellMin = new Vec2f(cellCoords).mul(cellExtents).add(grid_bound.min);
            Vec2f cellMax = new Vec2f(cellMin.x + cellExtents.x, cellMin.y + cellExtents.y);
            
            BBox cellBound = new BBox(cellMin, cellMax);
            
            cells.add(new MCellInfo(engine.getGraphicsContext(), cellBound));            
        }
        return cells;
    }
}
