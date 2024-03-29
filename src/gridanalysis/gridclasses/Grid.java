/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

import gridanalysis.coordinates.Vec2f;
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
       
    public Vec2f grid_inv()     {return new Vec2f(grid_dims()).div(bbox.extents());}   
    public Vec2f cell_size()    {return bbox.extents().div(new Vec2f(grid_dims()));}
    public Vec2i grid_dims()    {return dims.leftShift(shift);}
    public int grid_shift()     {return shift;}
    public Vec2f grid_min()     {return bbox.min.copy();}
    public Vec2f grid_max()     {return bbox.max.copy();}
    public BBox cellbound(Cell cell) {return new BBox(
                                                        bbox.min.add(cell_size().mul(new Vec2f(cell.min))), 
                                                        bbox.min.add(cell_size().mul(new Vec2f(cell.max))));}
}
