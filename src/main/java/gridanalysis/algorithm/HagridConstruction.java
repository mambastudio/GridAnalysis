/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.MEngine;
import java.util.Objects;

/**
 *
 * @author user
 */
public class HagridConstruction implements GridConstruction{
    private final MEngine engine;
    private final HagridConfig config;

    public HagridConstruction(MEngine engine)
    {
        this(engine, HagridConfig.defaults());
    }

    public HagridConstruction(MEngine engine, HagridConfig config)
    {
        this.engine = engine;
        this.config = Objects.requireNonNull(config, "config");
    }
    
    public Grid initialiseGrid(Tri[] tris)
    {
        HagridBuildContext context = new HagridBuildContext(config);
        
        build_grid(context, tris);
        merge_grid(context);
        flatten_grid(context);
        expand_grid(context, tris);
        if (config.compression()) compress_grid(context);
        
        return context.grid();
    }

    @Override
    public void build_grid(HagridBuildContext context, Tri[] tris) {
        Build build = new Build(engine, context);
        build.build_grid(tris, tris.length);
    }

    @Override
    public void merge_grid(HagridBuildContext context) {
        Merge build = new Merge(engine, context);
        build.merge_grid();
    }

    @Override
    public void flatten_grid(HagridBuildContext context) {
        Flatten flatten = new Flatten(engine, context);
        flatten.flatten_grid();
    }

    @Override
    public void expand_grid(HagridBuildContext context, Tri[] tris) {
        Expand expand = new Expand(engine, context);
        expand.expand_grid(tris, context.config().expansionIterations());
    }
    
}
