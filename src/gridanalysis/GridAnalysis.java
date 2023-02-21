/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis;

import gridanalysis.algorithm.EngineAbstract;
import gridanalysis.jfx.MBackground;
import gridanalysis.jfx.MCanvas;
import gridanalysis.jfx.MEngine;
import gridanalysis.jfx.MEngine2;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class GridAnalysis extends Application {
    
    private final MBackground background = new MBackground();
    private final MCanvas canvas = new MCanvas();
    private final EngineAbstract engine = new MEngine2();
    @Override
    public void start(Stage primaryStage) {
        
        engine.setGraphicsContext(canvas.getGraphicsContext2D());
        canvas.setEngine(engine);
        
        Pane root = new Pane(background, canvas);  
        
        //ensure they grow according to base draw panel
        background.prefWidthProperty().bind(root.widthProperty());
        background.prefHeightProperty().bind(root.heightProperty());        
        canvas.prefWidthProperty().bind(root.widthProperty());
        canvas.prefHeightProperty().bind(root.heightProperty());
        
        Scene scene = new Scene(root, 800, 650);        
        primaryStage.setTitle("Hagrid");
        primaryStage.setScene(scene);
        primaryStage.show();        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
