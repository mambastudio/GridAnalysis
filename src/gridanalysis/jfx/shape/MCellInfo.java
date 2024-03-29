/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.jfx.shape;

import gridanalysis.algorithm.GridAbstracts;
import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.irreg.BBox2;
import gridanalysis.irreg.Cell2;
import gridanalysis.jfx.MEngine;
import gridanalysis.utilities.Utility;
import gridanalysis.utilities.list.IntegerList;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author user
 */
public class MCellInfo {
    private final GraphicsContext ctx;
    private final double x;
    private final double y;
    private final double w;
    private final double h;
    
    public Object object;
    
    public MCellInfo(GraphicsContext context, BBox bbox)
    {
        this.ctx = context;
        this.x = bbox.min.x;
        this.y = bbox.min.y;
        this.w = bbox.extents().x;
        this.h = bbox.extents().y;
    }
    
    public MCellInfo(GraphicsContext context, BBox2 bbox)
    {
        this.ctx = context;
        this.x = bbox.min.x;
        this.y = bbox.min.y;
        this.w = bbox.extents().x;
        this.h = bbox.extents().y;        
    }
    
    public void draw()
    {
        ctx.save();
        
        ctx.strokeRect(x, y, w, h);
        if(object != null)
            drawText(object.toString());
        
        ctx.restore();
    }
    
    public static ArrayList<MCellInfo> getCells(MEngine engine, IntegerList cell_ids, BBox grid_bound,  Vec2i dims)
    {        
        ArrayList<MCellInfo> cells = new ArrayList();
        for(int i = 0; i<cell_ids.size(); i++)
        {
            if(cell_ids.get(i) < 0)
                continue;
            
            Vec2f cellExtents = Utility.getCellSize(dims, grid_bound);
            Vec2i cellCoords = Utility.getGridCoord(cell_ids.get(i), dims);
            
            Vec2f cellMin = new Vec2f(cellCoords).mul(cellExtents).add(grid_bound.min);
            Vec2f cellMax = new Vec2f(cellMin.x + cellExtents.x, cellMin.y + cellExtents.y);
            
            BBox cellBound = new BBox(cellMin, cellMax);
            
            cells.add(new MCellInfo(engine.getGraphicsContext(), cellBound));            
        }
        return cells;
    }
    
    public static ArrayList<MCellInfo> getCells2(MEngine engine, Cell2[] cellArray, BBox grid_bound,  Vec2i dims, int shift)
    {
        return null;
        
    }
    
    public static ArrayList<MCellInfo> getCells(MEngine engine, IntegerList cell_ids, IntegerList array_ids, BBox grid_bound,  Vec2i dims)
    {        
        ArrayList<MCellInfo> cells = new ArrayList();
        for(int i = 0; i<cell_ids.size(); i++)
        {
            int cell_id = cell_ids.get(i);
            
            if(cell_id < 0)
                continue;
            
            Vec2f cellExtents = Utility.getCellSize(dims, grid_bound);
            Vec2i cellCoords = Utility.getGridCoord(cell_id, dims);
            
            Vec2f cellMin = new Vec2f(cellCoords).mul(cellExtents).add(grid_bound.min);
            Vec2f cellMax = new Vec2f(cellMin.x + cellExtents.x, cellMin.y + cellExtents.y);
            
            BBox cellBound = new BBox(cellMin, cellMax);
            
            MCellInfo info = new MCellInfo(engine.getGraphicsContext(), cellBound);
            info.object = array_ids.get(i);
            cells.add(info);            
        }
        return cells;
    }
        
    public static ArrayList<MCellInfo> getCells(MEngine engine, Cell[] cellArray, BBox grid_bound,  Vec2i dims, int shift)
    {
        ArrayList<MCellInfo> cells = new ArrayList();
        for (Cell cell : cellArray) {
            
            Vec2f cell_size = Utility.getCellSize(dims.leftShift(shift), grid_bound);
                                   
            Vec2f cellMin = new Vec2f(cell.min).mul(cell_size).add(grid_bound.min);
            Vec2f cellMax = new Vec2f(cell.max).mul(cell_size).add(grid_bound.min);
            
            BBox cellBound = new BBox(cellMin, cellMax);
            
            //System.out.println("extents    "+cellBound.extents());
            //System.out.println("grid bound "+grid_bound.extents());
            //System.out.println("cell_min "+cellMin);
            
            MCellInfo info = new MCellInfo(engine.getGraphicsContext(), cellBound);
            info.object = cell.end;
            cells.add(info);
        }        
        return cells;
    }
    
    
    public static ArrayList<MCellInfo> getCells(MEngine engine, Cell[] cellArray, IntegerList array_ids, BBox grid_bound,  Vec2i dims, int shift)
    {
        ArrayList<MCellInfo> cells = new ArrayList();
        
        for (Cell cell : cellArray) {
           
            Vec2f cellExtents = Utility.getCellSize(dims.leftShift(shift), grid_bound);
            
            Vec2f cellMin = new Vec2f(cell.min).mul(cellExtents).add(grid_bound.min);
            Vec2f cellMax = new Vec2f(cell.max).mul(cellExtents).add(grid_bound.min);
            
            BBox cellBound = new BBox(cellMin, cellMax);
            
            MCellInfo info = new MCellInfo(engine.getGraphicsContext(), cellBound);
            info.object = cell.end - cell.begin;
            
            cells.add(info);            
        }        
        return cells;
    }
    
    public static ArrayList<MCellInfo> getCells(MEngine engine, Grid grid, BBox grid_bound, Vec2i dims, int shift)
    {
        ArrayList<MCellInfo> cells = new ArrayList();
        
        for (int i = 0; i<grid.num_cells; i++) {
            Cell cell = grid.cells.get(i);           
            {                
                BBox cellBound = grid.cellbound(cell);
                MCellInfo info = new MCellInfo(engine.getGraphicsContext(), cellBound);          
                
                info.object = cell.end - cell.begin;
                cells.add(info);             
            }
        }        
        return cells;
    }
    
       
    public static ArrayList<MCellInfo> getCells(MEngine engine, Cell[] cellArray, Object[] array_ids, BBox grid_bound,  Vec2i dims, int shift)
    {
        ArrayList<MCellInfo> cells = new ArrayList();
        int i = 0;
        for (Cell cell : cellArray) {
            Vec2f cellExtents = Utility.getCellSize(dims.leftShift(shift), grid_bound);
            
            Vec2f cellMin = new Vec2f(cell.min).mul(cellExtents).add(grid_bound.min);
            Vec2f cellMax = new Vec2f(cell.max).mul(cellExtents).add(grid_bound.min);
            
            BBox cellBound = new BBox(cellMin, cellMax);
            
            MCellInfo info = new MCellInfo(engine.getGraphicsContext(), cellBound);
            info.object = array_ids[i];
            
            cells.add(info);
            i++;
        }        
        return cells;
    }
    
    public void drawText(String text)
    {
        ctx.setFont(Font.font(10));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.setFill(Color.RED);
        ctx.fillText(text,
            x + w / 2,
            y + h / 2);
    }
    
    public static int lookup_entry(Entry[] entries, int shift, Vec2i dims, Vec2i voxel) {
        
        Entry entry = entries[(voxel.x >> shift) + dims.x * (voxel.y >> shift)];
        int log_dim = entry.log_dim, d = log_dim;
        while (log_dim != 0) {
            int begin = entry.begin;
            int mask = (1 << log_dim) - 1;

            //int k = (voxel >> int(shift - d)) & mask;
            Vec2i k = voxel.rightShift(shift -d).and(mask);
            entry = entries[begin + k.x + (k.y  << log_dim)];
            log_dim = entry.log_dim;
            d += log_dim;
        }
        return entry.begin;
    }
}
