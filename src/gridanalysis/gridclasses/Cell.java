/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

import gridanalysis.coordinates.Vec2i;

/**
 *
 * @author user
 * 
 * cells factor in the grid_shift (not sure if the same as log_dim)
 * 
 */
public class Cell {
    public Vec2i min;     ///< Minimum bounding box coordinate
    public int begin;     ///< Index of the first reference
    public Vec2i max;     ///< Maximum bounding box coordinate
    public int end;       ///< Past-the-end reference index

    public Cell() {}
    public Cell(Vec2i min, int begin, Vec2i max, int end)        
    {
        this.min = min;
        this.begin = begin;
        this.max = max;
        this.end = end;
    }
    
    @Override
    public final String toString() {
        return String.format("(min %1s, max %1s, begin %1s, end %1s)", min, max, begin, end);
    }
    
    public Cell copy()
    {
        return new Cell(min.copy(), begin, max.copy(), end);
    }
}
