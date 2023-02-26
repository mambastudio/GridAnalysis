/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import static gridanalysis.irreg.Float2.add;
import static gridanalysis.irreg.Float2.div;
import static gridanalysis.irreg.Float2.mul;
import static gridanalysis.irreg.Tri_Overlap_Box.tri_overlap_box;
import static gridanalysis.irreg.Voxel_Map.lookup_entry;
import java.util.ArrayList;

/**
 *
 * @author user
 */
public class Optimise_Overlap {
    
    public static boolean OVERLAP_SUBSET = false;
    
    // Finds the index of an element in a sorted array, return -1 if not found
    public static int bisection(IntegerList p, int c, int e) {
        // Cannot use std::binary_search here because we need the position within the array
        int a = 0, b = c - 1;
        while (a <= b) {
            int m = (a + b) / 2;
            int f = p.get(m);
            if (f == e) return m;
            a = (f < e) ? m + 1 : a;
            b = (f > e) ? m - 1 : b;
        }
        return -1;
    }
    
    // Determines if the second sorted array is a subset of the first
    public boolean is_subset(IntegerList p0, int c0, IntegerList p1, int c1) {
        return std::includes(p0, p0 + c0, p1, p1 + c1);
    }
    
    public void find_overlap(
                int axis,
                GridInfo info,
                IntegerList entries,
                IntegerList refs,
                ArrayList<Tri2> tris,
                ArrayList<Cell2> cells,
                int cell_id,
                Overlap overlap) {
        Cell2 cell = cells.get(cell_id);
        final int count = cell.end - cell.begin;
        final int axis1 = (axis + 1) % 2;
        
        Float2 cell_size = div(info.cell_size(), (1 << info.max_snd_dim));
        Float2 min_bb = add(info.bbox.min, mul(new Float2(cell.min[0], cell.min[1]), cell_size));
        Float2 max_bb = add(info.bbox.min, mul(new Float2(cell.max[0], cell.max[1]), cell_size));
        
        int dims[] = {
            info.dims[0] << info.max_snd_dim,
            info.dims[1] << info.max_snd_dim            
        };
        int dmin = 0, dmax = 0;
        
        if (cell.min[axis] > 0) {
            dmin = -dims[axis];
            int k1;
            for (int i = cell.min[axis1]; i < cell.max[axis1] && dmin < 0; i += k1) {
                k1 = dims[axis1];
                
                int xy[] = new int[2];
                xy[axis] = cell.min[axis] - 1;
                xy[axis1] = i;
                
                int entry = lookup_entry(entries, info.dims, info.max_snd_dim, xy[0], xy[1]);
                Cell2 next_cell = cells.get(entry);
                int next_count = next_cell.end - next_cell.begin;
                
                if(OVERLAP_SUBSET)
                {
                    if (is_subset(refs.getSublistFrom(cell.begin), count,
                              refs.getSublistFrom(next_cell.begin), next_count)) {
                        dmin = Math.max(dmin, next_cell.min[axis] - cell.min[axis]);
                    } else {
                        dmin = 0;
                        break;
                    }
                }
                else
                {
                    dmin = Math.max(dmin, next_cell.min[axis] - cell.min[axis]);

                    int first_ref = cell.begin;
                    for (int p = next_cell.begin; p < next_cell.end; p++) {
                        int ref = refs.get(p);
                        int found = bisection(refs.getSublistFrom(first_ref), cell.end - first_ref, ref);
                        first_ref = found + 1 + first_ref;
                        // If the reference is not in the cell we try to expand
                        if (found < 0) {
                            Tri2 tri = tris.get(ref);
                            Float2 cur_min = min_bb;
                            int a = dmin, b = -1;
                            // Using bisection, find the offset by which we can overlap the neighbour
                            while (a <= b) {
                                int m = (a + b) / 2;
                                cur_min.set(axis, info.bbox.min.get(axis) + cell_size.get(axis) * (cell.min[axis] + m));
                                if (tri_overlap_box(true, true, tri.v0, tri.e1, tri.e2, tri.normal(), cur_min, max_bb)) {
                                    a = m + 1;
                                } else {
                                    b = m - 1;
                                }
                            }
                            dmin = b + 1;
                            if (dmin == 0) break;
                        }
                    }
                    if (dmin == 0) break;
                }
                
               
            }
        }
    }
}
