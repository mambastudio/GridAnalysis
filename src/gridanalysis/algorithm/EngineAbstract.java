/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.jfx.shape.MCellInfo;
import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author jmburu
 */
public interface EngineAbstract {
    public void draw();
    public void drawMCellInfo();
    
    public void setGraphicsContext(GraphicsContext context);
    public GraphicsContext getGraphicsContext();
    
    public void setMCellInfo(ArrayList<MCellInfo>... cellInfoArray);    
}
