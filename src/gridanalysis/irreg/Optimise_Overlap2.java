/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import static gridanalysis.irreg.Voxel_Map.lookup_entry;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.utilities.list.ObjectList;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.stream.IntStream;

/**
 *
 * @author jmburu
 */
public class Optimise_Overlap2 {
    private static int[] grid_dims = null;
    private static int grid_shift = 0;
    
    public static boolean overlap_possible(int axis, boolean dir, Cell2 cell) {
        return dir ? cell.max[axis] < grid_dims[axis] : cell.min[axis] > 0;
    }
    
    /// Determines if the given range of references is a subset of the other
    public static boolean is_subset(IntegerList p0, int c0, IntegerList p1, int c1) {
        if (c1 > c0) return false;
        if (c1 == 0) return true;

        int i = 0, j = 0;

        do {
            int a = p0.get(i);
            int b = p1.get(j);
            if (b < a) return false;
            j += (a == b) ? 1 : 0;
            i++;
        } while (i < c0 & j < c1);

        return j == c1;
    }
    
    /// Finds the maximum overlap possible for one cell
    public static int find_overlap(
                            int axis,
                            boolean dir,
                            IntegerList entries,
                            IntegerList refs,
                            Tri2[] prims,
                            ObjectList<Cell2> cells,
                            Cell2 cell,
                            boolean continue_overlap) {
        int axis1 = (axis + 1) % 2;
        int axis2 = (axis + 2) % 2;

        if (!overlap_possible(axis, dir, cell)) return 0;

        int d = dir ? grid_dims[axis] : -grid_dims[axis];
        int k1, k2 = grid_dims[axis2];
        int i = cell.min[axis1];
        int j = cell.min[axis2];
        int max_d = d;
        
        while (true) {
            int[] next_cell = null;
            if (axis == 0) next_cell = new int[]{dir ? cell.max[0] : cell.min[0] - 1, i, j};
            if (axis == 1) next_cell = new int[]{j, dir ? cell.max[1] : cell.min[1] - 1};
            int entry = lookup_entry(entries, new int[]{grid_dims[0] >> grid_shift, grid_dims[1] >> grid_shift}, grid_shift, next_cell[0], next_cell[1]);
            Cell2 next = cells.get(entry);
            
            max_d = dir
                ? min(max_d, next.max[axis] - cell.max[axis])
                : max(max_d, next.min[axis] - cell.min[axis]);
            d = dir ? min(d, max_d) : max(d, max_d);
            
            if (!is_subset(refs.getSublistFrom(cell.begin), cell.end - cell.begin,
                           refs.getSublistFrom(next.begin), next.end - next.begin)) {
                d = 0;
                break;
            }
            
            k1 = next.max[axis1] - i;
            k2 = min(k2, next.max[axis2] - j);

            i += k1;
            if (i >= cell.max[axis1]) {
                i = cell.min[axis1];
                j += k2;
                k2 = grid_dims[axis2];
                if (j >= cell.max[axis2]) break;
            }
        }
        
        continue_overlap |= d == max_d;
        return d;
    }
    
    public static void overlap_step(
                            int axis,
                            IntegerList entries,
                            IntegerList refs,
                            Tri2[] prims,
                            ObjectList<Cell2> cells,
                            ObjectList<Cell2> new_cells,
                            IntegerList cell_flags, 
                            int num_cells) {
        IntStream.range(0, num_cells)
                .forEach(id->{                    
                    if (id >= num_cells || (cell_flags.get(id) & (1 << axis)) == 0)
                        return;
                    
                    Cell2 cell = cells.get(id).copy();
                    boolean flag = false;
                    int ov1 = find_overlap(axis, false, entries, refs, prims, cells, cell, flag);
                    int ov2 = find_overlap(axis, true, entries, refs, prims, cells, cell, flag);
                    
                    if (axis == 0) {
                        cell.min[0] += ov1;
                        cell.max[0] += ov2;                        
                    }
                   
                    if (axis == 1) {
                        cell.min[1] += ov1;
                        cell.max[1] += ov2;
                        
                    }

                    // If the cell has not been expanded, we will not process it next time
                    cell_flags.set(id, (flag ? 1 << axis : 0) | (cell_flags.get(id) & ~(1 << axis)));
                    new_cells.set(id, cell);
                    
                });
        //System.out.println(new_cells);
    }
    
    public static void expansion_iter(
                            GridInfo info, 
                            IntegerList entries,
                            IntegerList refs,
                            Tri2[] prims,
                            ObjectList<Cell2> cells,
                            ObjectList<Cell2> new_cells,
                            IntegerList cell_flags) {
        int num_cells = cells.size();
        overlap_step(0, entries, refs, prims, cells, new_cells, cell_flags, num_cells);       
        new_cells.swap(cells);

        overlap_step(1, entries, refs, prims, cells, new_cells, cell_flags, num_cells);
        new_cells.swap(cells); 
    }
    
    public static void optimise_overlap(
                GridInfo info,
                IntegerList entries,
                IntegerList refs,
                Tri2[] tris,
                ObjectList<Cell2> cells) {
        
        int iters = 1;
        
        ObjectList<Cell2> new_cells  = new ObjectList(cells.size(), ()-> new Cell2());
        IntegerList cell_flags = new IntegerList(new int[cells.size()]);
        cell_flags.fill(1);
        
        grid_dims = new int[]{
            info.dims[0] << info.max_snd_dim,
            info.dims[1] << info.max_snd_dim
        };
        grid_shift = info.max_snd_dim;
        
        for (int i = 0; i < iters; i++)
            expansion_iter(info, entries, refs, tris, cells, new_cells, cell_flags);
    }
}
