/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.utilities.list.ObjectList;

/**
 *
 * @author user
 */
public class Grid {
    public Entry[] entries;                ///< Voxel map, stored as a contiguous array
    public IntegerList   ref_ids;             ///< Array of primitive references
    public ObjectList<Cell>  cells;                  ///< Cells of the structure (nullptr if compressed)

    //SmallCell* small_cells;       ///< Compressed cells (nullptr if not compressed)

    public BBox bbox;                      ///< Bounding box of the scene
    public Vec2i dims;                     ///< Top-level dimensions
    public int num_cells;                  ///< Number of cells
    public int num_entries;                ///< Number of elements in the voxel map
    public int num_refs;                   ///< Number of primitive references
    public int shift;                      ///< Amount of bits to shift to get from the deepest level to the top-level
    public IntegerList offsets;               ///< Offset to each level of the voxel map octree
    
    
    //get cell index
    public int lookup_entry(Cell cell)
    {
        Vec2i voxel = new Vec2i(cell.min);
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
