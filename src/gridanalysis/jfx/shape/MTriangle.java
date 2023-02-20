/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx.shape;

import gridanalysis.gridclasses.Tri;
import gridanalysis.irreg.Tri2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author user
 */
public class MTriangle {
    private Tri tri;
    private Tri2 tri2;
    
    private final GraphicsContext ctx;
    
    double[] xPoints, yPoints;
    int nPoints = 3;
    
    public MTriangle(GraphicsContext context, Tri tri)
    {
        this.ctx = context;
        this.tri = tri;
        
        xPoints = new double[nPoints];
        yPoints = new double[nPoints];
        
        xPoints[0] = this.tri.p0().x;
        yPoints[0] = this.tri.p0().y;
        
        xPoints[1] = this.tri.p1().x;
        yPoints[1] = this.tri.p1().y;
        
        xPoints[2] = this.tri.p2().x;
        yPoints[2] = this.tri.p2().y;
    }
    
    public MTriangle(GraphicsContext context, Tri2 tri)
    {
        this.ctx = context;
        this.tri2 = tri;
        
        xPoints = new double[nPoints];
        yPoints = new double[nPoints];
        
        xPoints[0] = tri2.p0().x;
        yPoints[0] = tri2.p0().y;
        
        xPoints[1] = tri2.p1().x;
        yPoints[1] = tri2.p1().y;
        
        xPoints[2] = tri2.p2().x;
        yPoints[2] = tri2.p2().y;
    }
    
    public void draw()
    {
        ctx.save();
        
        ctx.setFill(Color.BISQUE);
        ctx.fillPolygon(xPoints, yPoints, nPoints);
        ctx.strokePolygon(xPoints, yPoints, nPoints);
        
        ctx.restore();
    }
}
