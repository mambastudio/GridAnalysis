/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.math.MTransform;
import gridanalysis.jfx.shape.MTriangle;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author user
 */
public class MEngine {
    MTransform transform = MTransform.translate(200, 200);
    GraphicsContext ctx;
    Tri tri;// = new Tri(new Vec2f(0, 0), new Vec2f(0, 60), new Vec2f(60, 60));
    
    public MEngine()
    {
        float x_min = 0, y_min = 0, x_max = 100, y_max = 100;
        
        float x1 = randomFloat(x_min, x_max);
        float y1 = randomFloat(y_min, y_max);
        float x2 = randomFloat(x_min, x_max);
        float y2 = randomFloat(y_min, y_max);
        float x3 = randomFloat(x_min, x_max);
        float y3 = randomFloat(y_min, y_max);
        
        tri = new Tri(new Vec2f(x1, y1), new Vec2f(x2, y2), new Vec2f(x3, y3));
    }
    
    public void draw()
    {
        ctx.save();
        transform.transformGraphicsContext(ctx);
        MTriangle mtri = new MTriangle(ctx, tri);
        mtri.draw();
        ctx.restore();
    }
    
    public void setGraphicsContext(GraphicsContext context)
    {
        this.ctx = context;
    }
    
    private float randomFloat(float min, float max)
    {
        return (float) (min + Math.random() * (max - min));
    }
}
