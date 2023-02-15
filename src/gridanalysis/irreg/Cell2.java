/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import gridanalysis.utilities.IntArray;

/**
 *
 * @author jmburu
 */
/// A cell in the grid. Contains its boundaries in voxel coordinates, along with the range of primitives it contains.
public class Cell2 {
    public int min[] = new int[3];
    public int begin;
    public int max[] = new int[3];
    public int end;
    
    // Traversal cost for one cell
    public static float unit_cost = 1.0f;
    // Cost of intersecting n triangles
    public static int K(int n) {
        return (n + 1) / 2;
    }
    
    public boolean is_merge_profitable(Float2 cell_size, IntArray refs, Cell2 other) {
        float cost0 = cost(cell_size);
        float cost1 = other.cost(cell_size);
        int merged_max[] = { Math.max(max[0], other.max[0]),
                              Math.max(max[1], other.max[1])};
        int merged_min[] = { Math.min(min[0], other.min[0]),
                              Math.min(min[1], other.min[1])};
        float merged_area = half_area(cell_size, merged_min, merged_max);
        int count = Grid2.count_union(refs.splitSubArrayFrom(begin), end - begin,
                                      refs.splitSubArrayFrom(other.begin), other.end - other.begin);
        return merged_area * (K(count) + unit_cost) <= (cost0 + cost1);
    }
    
    public float cost(Float2 cell_size) {
        return half_area(cell_size, min, max) * (K(end - begin) + unit_cost);
    }
    
    public boolean can_merge(int axis, Cell2 other) {
        int axis1 = (axis + 1) % 2;
        return other.min[axis]  == max[axis]  &&
               other.min[axis1] == min[axis1] && other.max[axis1] == max[axis1];
    }
    
    public static float half_area(Float2 cell_size, int[] min, int[] max) {
        Float2 extents = new Float2((max[0] - min[0]) * cell_size.x,
                                    (max[1] - min[1]) * cell_size.y);
        return extents.x * extents.y;
    }
}
