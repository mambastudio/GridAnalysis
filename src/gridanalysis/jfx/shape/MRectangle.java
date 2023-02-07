/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx.shape;

import gridanalysis.gridclasses.BBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author user
 */
public class MRectangle {
    
    private final GraphicsContext ctx;
    private final double x;
    private final double y;
    private final double w;
    private final double h;
    
    public MRectangle(GraphicsContext context, BBox bbox)
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
        ctx.setStroke(Color.DARKGREEN);
        ctx.strokeRect(x, y, w, h);
        
        ctx.restore();
    }
}
