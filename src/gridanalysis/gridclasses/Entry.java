/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.coordinates.Vec4i;

/**
 *
 * @author user
 */
public class Entry {
    public int log_dim;    ///< Logarithm of the dimensions of the entry (0 for leaves)
    public int begin;      ///< Next entry index (cell index for leaves)
    
    public Entry(){
        log_dim = 0;
        begin = 0;
    }
    
    public Entry(int log_dim, int begin)
    {
        this.log_dim = log_dim;
        this.begin = begin;
    }
    
    public Vec4i asVec4i()
    {
        return new Vec4i(log_dim, begin);
    }
    
    public Vec2i asVec2i()
    {
        return new Vec2i(log_dim, begin);
    }
    
    public Entry copy()
    {
        return new Entry(log_dim, begin);
    }
    
    @Override
    public String toString()
    {
        //return "log_dim " +log_dim+ "\n"
        //     + " begin " +begin;
        return "" +log_dim;
    }
}
