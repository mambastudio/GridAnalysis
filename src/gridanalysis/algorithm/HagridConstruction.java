/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.MEngine;

/**
 *
 * @author user
 */
public class HagridConstruction implements GridConstruction{
    private final MEngine engine;
    public HagridConstruction(MEngine engine)
    {
        this.engine = engine;
    }
    
    public Grid initialiseGrid(Tri[] tris)
    {
        Hagrid hagrid = new Hagrid();
        
        build_grid(hagrid, tris);
        merge_grid(hagrid);
        flatten_grid(hagrid);
        
        return hagrid.getIrregularGrid();
    }

    @Override
    public void build_grid(Hagrid hagrid, Tri[] tris) {
        Build build = new Build(engine, hagrid);
        build.build_grid(tris, tris.length);
    }

    @Override
    public void merge_grid(Hagrid hagrid) {
        //Merge build = new Merge(engine, hagrid);
        //build.merge_grid();
    }

    @Override
    public void flatten_grid(Hagrid hagrid) {
        //Flatten flatten = new Flatten(engine, hagrid);
        //flatten.flatten_grid();
    }
    
}
