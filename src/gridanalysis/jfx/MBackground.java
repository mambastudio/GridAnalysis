/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 *
 * @author user
 */
public class MBackground extends Pane {
    private static final double SPACING_X = 15;
    private static final double SPACING_Y = 10;
    private static final double RADIUS = 1.5;
    private final Canvas canvas = new Canvas();
 
    public MBackground() {
        getChildren().add(canvas);
        
    }
 
    @Override protected void layoutChildren() 
    {
        final int top = (int)snappedTopInset();
        final int right = (int)snappedRightInset();
        final int bottom = (int)snappedBottomInset();
        final int left = (int)snappedLeftInset();
        final int w = (int)getWidth() - left - right;
        final int h = (int)getHeight() - top - bottom;
        canvas.setLayoutX(left);
        canvas.setLayoutY(top);
        if (w != canvas.getWidth() || h != canvas.getHeight()) {
            canvas.setWidth(w);
            canvas.setHeight(h);
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, w, h);
            
            g.setFill(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.setFill(Color.gray(0.5,0.5));
 
            for (int x = 0; x < w; x += SPACING_X) {
                for (int y = 0; y < h; y += SPACING_Y) {
                    double offsetY = (y%(2*SPACING_Y)) == 0 ? SPACING_X /2 : 0;
                    g.fillOval(x-RADIUS+offsetY,y-RADIUS,RADIUS+RADIUS,RADIUS+RADIUS);
                }
            }
        }
    }
    
}

