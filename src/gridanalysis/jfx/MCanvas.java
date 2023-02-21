/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx;

import gridanalysis.algorithm.EngineAbstract;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;

/**
 *
 * @author user
 */
public class MCanvas extends Region{
    private final Canvas canvas;
    private EngineAbstract engine;
    
    public MCanvas()
    {
        double width = 50, height = 50;
        //set the width and height of this and the canvas as the same
        //set the width and height of this and the canvas as the same
        setWidth(width);
        setHeight(height);
        canvas = new Canvas(width, height);
        //add the canvas as a child
        getChildren().add(canvas);
        //bind the canvas width and height to the region
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        canvas.widthProperty().addListener((o, oV, nv)->{
            if(engine != null)
                engine.draw();
        });
        canvas.heightProperty().addListener((o, oV, nv)->{
            if(engine != null)
                engine.draw();
        });
    }
    
    public GraphicsContext getGraphicsContext2D() {
        return canvas.getGraphicsContext2D();
    }
    
    public void setEngine(EngineAbstract engine)
    {
        this.engine = engine;
    }
}
