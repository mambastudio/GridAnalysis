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
import gridanalysis.gridclasses.Tri;

/**
 *
 * @author user
 */
public class Hagrid implements HagridConstruction {
    //grid info (temporary data set used during building)
    public Vec2i grid_dims;
    public BBox  grid_bbox;
    public Vec2f cell_size;
    public int   grid_shift;
    
    //grid accelerating structure
    public Grid irregular_grid;
    
    public Grid getIrregularGrid()
    {
        return irregular_grid;
    }

    @Override
    public void build_grid(Tri[] tris, int num_tris, Grid grid, float top_density, float snd_density) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void merge_grid(Grid grid, float alpha) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flatten_grid(Grid grid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void expand_grid(Grid grid, Tri[] tris, int iters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean compress_grid(Grid grid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
