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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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
    public static boolean is_subset(IntegerList p0, int c0, IntegerList p1, int c1) {
        //return std::includes(p0, p0 + c0, p1, p1 + c1);
        return Common.isSubset(p0.getSublistFrom(c0), p1.getSublistFrom(c1));
    }
    
    public static boolean overlap_possible(int axis, boolean dir, int[] grid_dims, Cell2 cell) {
        return dir ? cell.max[axis] < grid_dims[axis] : cell.min[axis] > 0;
    }
    
    /*
    /// Computes the amount of overlap possible for a cell and a given primitive    
    public static int compute_overlap(int axis, boolean dir, Tri2 prim, Float2 grid_min, Cell2 cell, BBox2 cell_bbox, int d) {
        int axis1 = (axis + 1) % 2;
        BBox2 prim_bbox = prim.bbox();

        if ((prim_bbox.min.get(axis1)) <= cell_bbox.max.get(axis1) &&
            (prim_bbox.max.get(axis1)) >= cell_bbox.min.get(axis1)) {
            // Approximation: use the original bounding box, not the clipped one
            int prim_d = ((dir ? (prim_bbox.min.get(axis)) : (prim_bbox.max.get(axis))) - (grid_min.get(axis))) * get<axis>(grid_inv);
            d = dir
                ? min(d, prim_d - get<axis>(cell.max))
                : max(d, prim_d - get<axis>(cell.min) + 1);
            d = dir ? max(d, 0) : min(d, 0);
        }
        return d;
    }
*/
    
    public static void find_overlap(
                int axis,
                GridInfo info,
                IntegerList entries,
                IntegerList refs,
                Tri2[] tris,
                ArrayList<Cell2> cells,
                int cell_id,
                Overlap overlap) {
        if(true)return;
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
                                    
                System.out.println("kubafu");
                int xy[] = new int[3];
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
                            Tri2 tri = tris[ref];
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
                k1 = Math.min(k1, next_cell.max[axis1] - i);
                
            }
        }
        if (cell.max[axis] < dims[axis]) {
            dmax = dims[axis];

            int k1;
            for (int i = cell.min[axis1]; i < cell.max[axis1] && dmax > 0; i += k1) {
                k1 = dims[axis1];
                
                    
                int xyz[] = new int[2];
                xyz[axis] = cell.max[axis];
                xyz[axis1] = i;

                int entry = lookup_entry(entries, info.dims, info.max_snd_dim, xyz[0], xyz[1]);
                Cell2 next_cell = cells.get(entry);
                int next_count = next_cell.end - next_cell.begin;

                if(OVERLAP_SUBSET)
                {
                    if (is_subset(refs.getSublistFrom(cell.begin), count,
                                  refs.getSublistFrom(next_cell.begin), next_count)) {
                        dmax = Math.min(dmax, next_cell.max[axis] - cell.max[axis]);
                    } else {
                        dmax = 0;
                        break;
                    }
                }
                else
                {
                    dmax = Math.min(dmax, next_cell.max[axis] - cell.max[axis]);

                    int first_ref = cell.begin;
                    for (int p = next_cell.begin; p < next_cell.end; p++) {
                        int ref = refs.get(p);
                        int found = bisection(refs.getSublistFrom(first_ref), cell.end - first_ref, ref);
                        first_ref = found + 1 + first_ref;
                        // If the reference is not in the cell we try to expand
                        if (found < 0) {
                            Tri2 tri = tris[ref];
                            Float2 cur_max = max_bb;
                            int a = 1, b = dmax;
                            // Using bisection, find the offset by which we can overlap the neighbour
                            while (a <= b) {
                                int m = (a + b) / 2;
                                cur_max.set(axis, info.bbox.min.get(axis) + cell_size.get(axis) * (cell.max[axis] + m));
                                if (tri_overlap_box(true, true, tri.v0, tri.e1, tri.e2, tri.normal(), min_bb, cur_max)) {
                                    b = m - 1;
                                } else {
                                    a = m + 1;
                                }
                            }
                            dmax = a - 1;
                            if (dmax == 0) break;
                        }
                    }
                    if (dmax == 0) break;
                }
                k1 = Math.min(k1, next_cell.max[axis1] - i);
                
            }
        }
        
        overlap.dmin = dmin;
        overlap.dmax = dmax;
    }
    
    public static int optimize_overlap(
                    GridInfo info,
                    IntegerList entries,
                    IntegerList refs,
                    Tri2[] tris,
                    boolean[] cell_flags,
                    ArrayList<Cell2> cells) {
        AtomicInteger overlaps = new AtomicInteger(0);
        
        IntStream.range(0, cells.size())
                .forEach(i->{
                    
                    
                    if (!cell_flags[i]) return;
                    Cell2 cell = cells.get(i);
                    int k = 0;
                    
                    Overlap overlap = new Overlap();
                    find_overlap(0, info, entries, refs, tris, cells, i, overlap);
                    cell.min[0] += overlap.dmin;
                    cell.max[0] += overlap.dmax;
                    k += (overlap.dmin < 0 | overlap.dmax > 0) ? 1 : 0;
                    
                    find_overlap(1, info, entries, refs, tris, cells, i, overlap);
                    cell.min[1] += overlap.dmin;
                    cell.max[1] += overlap.dmax;
                    k += (overlap.dmin < 0 | overlap.dmax > 0) ? 1 : 0;
                    
                    cell_flags[i] = k != 0;
                    overlaps.addAndGet(k);
                });
        return overlaps.get();
    }
}
