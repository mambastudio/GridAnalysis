/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Grid;

/**
 *
 * @author user
 */
public class Hagrid {
    //grid info (temporary data set used during building)
    public Vec2i grid_dims;
    public BBox  grid_bbox;
    public Vec2f cell_size;
    public int   grid_shift;
    
    //grid accelerating structure
    public Grid grid;
}
