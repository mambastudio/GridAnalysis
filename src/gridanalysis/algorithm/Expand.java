/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.MEngine;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.utilities.list.ObjectList;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 *
 * @author user
 */
public class Expand extends GridAbstracts{
    private final MEngine engine;
    
    Vec2i   grid_dims;
    Vec2f   grid_min;
    Vec2f   cell_size;
    Vec2f   grid_inv;
    int     grid_shift;
    
    public Expand(MEngine engine)
    {
        this.engine = engine;
    }
    
    /// Returns true if an overlap with a neighboring cell is possible    
    public boolean overlap_possible(int axis, boolean dir, Cell cell) {
        if (dir)
            return cell.max.get(axis) < grid_dims.get(axis);
        else
            return cell.min.get(axis) > 0;
    }
    
    /// Determines if the given range of references is a subset of the other
    public boolean is_subset(IntegerList p0, int c0, IntegerList p1, int c1) {
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
    
    /// Computes the amount of overlap possible for a cell and a given primitive    
    public boolean compute_overlap(int axis, boolean dir, Tri prim, Cell cell, BBox cell_bbox, int d) {
        int axis1 = (axis + 1) % 3;
        int axis2 = (axis + 2) % 3;
        BBox prim_bbox = prim.bbox();

        if (prim_bbox.min.get(axis1) <= cell_bbox.max.get(axis1) &&
            prim_bbox.max.get(axis1) >= cell_bbox.min.get(axis1) &&
            prim_bbox.min.get(axis2) <= cell_bbox.max.get(axis2) &&
            prim_bbox.max.get(axis2) >= cell_bbox.min.get(axis2)) {
            // Approximation: use the original bounding box, not the clipped one
            int prim_d =(int) (((dir ? prim_bbox.min.get(axis) : prim_bbox.max.get(axis))
                    - grid_min.get(axis)) * grid_inv.get(axis));
            d = dir
                ? min(d, prim_d - (cell.max.get(axis)))
                : max(d, prim_d - (cell.min.get(axis)) + 1);
            d = dir ? max(d, 0) : min(d, 0);
        }
        return d != 0;
    }
    
    /// Finds the maximum overlap possible for one cell    
    public int find_overlap(
                                int axis, boolean dir, boolean subset_only,
                                Entry[] entries,
                                IntegerList refs,
                                Tri[] prims,
                                Cell[] cells,
                                Cell cell,
                                boolean continue_overlap) {
        int axis1 = (axis + 1) % 3;
        int axis2 = (axis + 2) % 3;

        if (!overlap_possible(axis, dir, cell)) return 0;

        int d = dir ? (grid_dims.get(axis)) : -(grid_dims.get(axis));
        int k1, k2 = (grid_dims.get(axis2));
        int i = cell.min.get(axis1);
        int j = cell.min.get(axis2);
        int max_d = d;
        while (true) {
            Vec2i next_cell;
            if (axis == 0) next_cell = new Vec2i(dir ? cell.max.x : cell.min.x - 1, i);
            else next_cell = new Vec2i(j, dir ? cell.max.y : cell.min.y - 1);
            int entry = lookup_entry(entries, grid_shift, grid_dims.rightShift(grid_shift), next_cell);
            Cell next = cells[entry];

            max_d = dir
                ? min(max_d, next.max.get(axis) - cell.max.get(axis))
                : max(max_d, next.min.get(axis) - cell.min.get(axis));
            d = dir ? min(d, max_d) : max(d, max_d);

            if (subset_only) {
                if (!is_subset(refs.getSubListFrom(cell.begin), cell.end - cell.begin,
                               refs.getSubListFrom(next.begin), next.end - next.begin)) {
                    d = 0;
                    break;
                }
            } else {
                if (next.begin < next.end) {
                    BBox cell_bbox = new BBox(
                            grid_min.add(cell_size.mul(new Vec2f(cell.min))),
                            grid_min.add(cell_size.mul(new Vec2f(cell.max))));

                    int p1 = cell.begin, p2 = next.begin;
                    int ref2 = refs.get(p2);
                    while (true) {
                        // Skip references that are present in the current cell
                        while (p1 < cell.end) {
                            int ref1 = refs.get(p1);

                            if (ref1  > ref2) break;
                            if (ref1 == ref2) {
                                if (++p2 >= next.end) break;
                                ref2 = refs.get(p2);
                            }

                            p1++;
                        }

                        if (p2 >= next.end) break;

                        // Process references that are only present in the next cell
                        d = compute_overlap(axis, dir, prims[ref2], cell, cell_bbox, d) ? 1 : 0;
                        if (d == 0 || ++p2 >= next.end) break;
                        ref2 = refs.get(p2);
                    }
                }

                if (d == 0) break;
            }

            k1 = next.max.get(axis1) - i;
            k2 = min(k2, next.max.get(axis2) - j);

            i += k1;
            if (i >= (cell.max.get(axis1))) {
                i = (cell.min.get(axis1));
                j += k2;
                k2 = (grid_dims.get(axis2));
                if (j >= (cell.max.get(axis2))) break;
            }
        }
        continue_overlap |= d == max_d;
        return d;
    }

    
    public void overlap_step(    
                                int axis,
                                Entry[]     entries,
                                IntegerList       refs,
                                Tri[] prims,
                                Cell[]      cells,
                                Cell[]      new_cells,
                                IntegerList       cell_flags,
                                int num_cells) {
        
        for(int id = 0; id<num_cells; id++)
        {
            
            if (id >= num_cells || (cell_flags.get(id) & (1 << axis)) == 0)
                return;

            Cell cell = cells[id].copy();
            boolean flag = false;
            boolean subset_only = true;
            int ov1 = find_overlap(axis, false, subset_only, entries, refs, prims, cells, cell, flag);
            int ov2 = find_overlap(axis, true, subset_only, entries, refs, prims, cells, cell, flag);

            if (axis == 0) {
                cell.min.x += ov1;
                cell.max.x += ov2;
            }

            if (axis == 1) {
                cell.min.y += ov1;
                cell.max.y += ov2;
            }

            // If the cell has not been expanded, we will not process it next time
            cell_flags.set(id, (flag ? 1 << axis : 0) | (cell_flags.get(id) & ~(1 << axis)));
            new_cells[id] = cell;
        }
    }
        
    public void expansion_iter(Grid grid, Tri[] prims, ObjectList<Cell[]> new_cells, IntegerList cell_flags) {
        overlap_step(0, grid.entries, grid.ref_ids, prims, grid.cells.get(), new_cells.get(), cell_flags, grid.num_cells);
        new_cells.swap(grid.cells);
                
        overlap_step(1, grid.entries, grid.ref_ids, prims, grid.cells.get(), new_cells.get(), cell_flags, grid.num_cells);
        new_cells.swap(grid.cells);
    }
       
    void expand(Grid grid, Tri[] prims, int iters) {
        if (iters == 0) return;

        Cell[] new_cells  = new Cell[grid.num_cells];
        IntegerList cell_flags = new IntegerList(new int[grid.num_cells]);

        cell_flags.fillOne(grid.num_cells);
        Vec2f extents = grid.bbox.extents();
        Vec2i dims = grid.dims.leftShift(grid.shift);
        Vec2f cell_size = extents.div(new Vec2f(dims));
        Vec2f grid_inv = new Vec2f(dims).div(extents);

        this.grid_dims = dims;
        this.grid_min  = grid.bbox.min;
        this.cell_size = cell_size;
        this.grid_inv  = grid_inv;
        this.grid_shift= grid.shift;

        for (int i = 0; i < iters; i++)
            expansion_iter(grid, prims, new ObjHolder(new_cells), cell_flags);
    }
    
    public void expand_grid(Grid grid, Tri[] tris, int iters) 
    { 
        expand(grid, tris, iters); 
    }
}
