/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import gridanalysis.utilities.list.IntegerList;

/**
 *
 * @author user
 */
public class Voxel_Map {
    /// Number of bits to allocate to store the sub-level dimensions in the voxel map.
    public static int ENTRY_SHIFT = 4;
    
    /// Returns the log of the dimension in stored in the top-level given voxel map entry.
    public static int entry_log_dim(int entry) {
        return entry & ((1 << ENTRY_SHIFT) - 1);
    }

    /// Returns the pointer to the second-level stored in the given top-level voxel map entry.
    public static int entry_begin(int entry) {
        return entry >> ENTRY_SHIFT;
    }

    /// Creates a top-level voxel map entry.
    public static int make_entry(int begin, int log_dim) {
        assert(log_dim < (1 << ENTRY_SHIFT) &&
               begin < (1 << (32 - ENTRY_SHIFT)));
        return (begin << ENTRY_SHIFT) | log_dim;
    }

    /// Returns the number of second-level entries for the given top-level voxel map entry.
    public static int leaf_count(int entry) {
        int dim = 1 << entry_log_dim(entry);
        return dim * dim * dim;
    }

    /// Lookups an entry in the voxel map.
    public static int lookup_entry(IntegerList entries,  int[] coarse_dims, int shift, int x, int y) {
        int entry = entries.get((x >> shift) + coarse_dims[0] * ((y >> shift)));
        int log_dim = entry_log_dim(entry);
        int mask = (1 << log_dim) - 1;
        int begin = entry_begin(entry);
        int kx = (x >> (shift - log_dim)) & mask;
        int ky = (y >> (shift - log_dim)) & mask;
        return entries.get(begin + kx + (ky << log_dim));
    }
}
