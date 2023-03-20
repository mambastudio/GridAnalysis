/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.Entry;

/**
 *
 * @author user
 */
public abstract class GridAbstracts {
    public int __ffs(int value)
    {
        int pos = 1;
        while ((value & 1) == 0 && value != 0) {
            value >>= 1;
            pos++;
        }
        return (value == 0) ? 0 : pos;
    }
    
    public int __popc(int mask) {
        return Integer.bitCount(mask);
    }
    
    /// Returns a voxel map entry with the given dimension and starting index
    public Entry make_entry(int log_dim, int begin) {
        Entry e = new Entry(log_dim, begin);
        return e;
    }
    
    public int lookup_entry(Entry[] entries, int shift, Vec2i dims, Vec2i voxel) {
        
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
