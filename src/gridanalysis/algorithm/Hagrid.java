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
public class Hagrid{
    public float top_density = 0.12f;
    public float snd_density = 0.2f;
    public float alpha = 0.995f;
    public int exp_iters = 3;
    
    //grid info (temporary data set used during building)
    public Vec2i grid_dims;
    public BBox  grid_bbox;
    public Vec2f cell_size;
    public int   grid_shift;
    
    //grid accelerating structure
    public Grid irregular_grid;
    
    public Hagrid()
    {
        grid_dims = new Vec2i();
        grid_bbox = new BBox();
        cell_size = new Vec2f();
        
        irregular_grid = new Grid();
    }
    
    public Grid getIrregularGrid()
    {
        return irregular_grid;
    }
}
