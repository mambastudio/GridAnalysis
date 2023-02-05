/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author user
 */
public class MEngine {
    GraphicsContext context;
    
    
    public void draw()
    {
        System.out.println("kubafu");
    }
    
    public void setGraphicsContext(GraphicsContext context)
    {
        this.context = context;
    }
}
