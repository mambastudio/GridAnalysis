/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import static gridanalysis.irreg.Float2.add;
import static gridanalysis.irreg.Float2.div;
import static gridanalysis.irreg.Float2.mul;
import static gridanalysis.irreg.Voxel_Map.lookup_entry;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.utilities.list.ObjectList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 *
 * @author user
 */
public class Optimise_Overlap {
    
    public static boolean OVERLAP_SUBSET = true;
    
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
        return Common.is_subset(p0, c0, p1, c1);
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
                ObjectList<Cell2> cells,
                int cell_id,
                Overlap overlap) {
        
        Cell2 cell = cells.get(cell_id);
        int count = cell.end - cell.begin;
        int axis1 = (axis + 1) % 2;
                
        int dims[] = info.max_subcells_dims();
        
        int dmin = 0, dmax = 0;
        
        if (cell.min[axis] > 0) {
            dmin = -dims[axis];

            int k1; 
            for (int i = cell.min[axis1]; i < cell.max[axis1] && dmin < 0; i += k1) {
                k1 = dims[axis1];
                
                int xy[] = new int[2];
                xy[0] = cell.min[axis] - 1;
                xy[1] = i;

                int entry = lookup_entry(entries, info.dims, info.max_snd_dim, xy[0], xy[1]);
                Cell2 next_cell = cells.get(entry);
                int next_count = next_cell.end - next_cell.begin;

                if (is_subset(refs.getSublistFrom(cell.begin), count,
                              refs.getSublistFrom(next_cell.begin), next_count)) {
                    dmin = Math.max(dmin, next_cell.min[0] - cell.min[0]);
                } else {
                    dmin = 0;                    
                }                
                k1 = Math.min(k1, next_cell.max[axis1] - i);         
                System.out.println("asdfa");                
             
            }
        }
        
        if (cell.max[axis] < dims[axis]) {
            dmax = dims[axis];
            
            int k1; 
            for (int i = cell.min[axis1]; i < cell.max[axis1] && dmax > 0; i += k1) {
                k1 = dims[axis1];               

                int xy[] = new int[2];
                xy[0] = cell.max[axis];
                xy[1] = i;
               // System.out.println(dmax);
                int entry = lookup_entry(entries, info.dims, info.max_snd_dim, xy[0], xy[1]);
                Cell2 next_cell = cells.get(entry);
                int next_count = next_cell.end - next_cell.begin;

                if (is_subset(refs.getSublistFrom(cell.begin), count,
                          refs.getSublistFrom(next_cell.begin), next_count)) {
                    dmax = Math.min(dmax, next_cell.max[axis] - cell.max[axis]);
                } else {
                    dmax = 0;                   
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
                    ObjectList<Cell2> cells) {
        AtomicInteger overlaps = new AtomicInteger(0);
        
        int axis = 0;
        int axis1 = 1;
        int k1 = 0;
        
        Cell2 cel = cells.get(5);
        
        int p = cel.min[axis1];
        
        int xy[] = new int[2];
        xy[0] = cel.max[axis];
        xy[1] = p;
                
        
        int entry = lookup_entry(entries, info.dims, info.max_snd_dim, xy[0], xy[1]);
        Cell2 next_cell = cells.get(entry);
        
        System.out.println(entry);
        
        int kk = next_cell.max[axis1] - p;
        xy[0] = cel.max[axis];
        xy[1] = p+kk;       
        
        entry = lookup_entry(entries, info.dims, info.max_snd_dim, xy[0], xy[1]);
        System.out.println(entry);
        
        
        IntStream.range(0, cells.size())
                .forEach(i->{                    
                    
                    if (!cell_flags[i]) return;
                    Cell2 cell = cells.get(i);
                    int k = 0;
                    
                    //Overlap overlap = new Overlap();
                    //find_overlap(0, info, entries, refs, tris, cells, i, overlap);
                    //cell.min[0] += overlap.dmin;
                    //cell.max[0] += overlap.dmax;
                    //k += (overlap.dmin < 0 | overlap.dmax > 0) ? 1 : 0;
                    
                    //find_overlap(1, info, entries, refs, tris, cells, i, overlap);
                   // cell.min[1] += overlap.dmin;
                    //cell.max[1] += overlap.dmax;
                   // k += (overlap.dmin < 0 | overlap.dmax > 0) ? 1 : 0;
                                       
                    cell_flags[i] = k != 0;
                    overlaps.addAndGet(k);
                });
        return overlaps.get();
    }
}
