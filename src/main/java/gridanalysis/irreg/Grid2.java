/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import gridanalysis.utilities.list.IntegerList;
import static gridanalysis.irreg.Common.clamp;
import static gridanalysis.irreg.Common.closest_log2;
import static gridanalysis.irreg.Common.partition;
import static gridanalysis.irreg.Float2.add;
import static gridanalysis.irreg.Float2.addAll;
import static gridanalysis.irreg.Float2.div;
import static gridanalysis.irreg.Float2.mul;
import static gridanalysis.irreg.Tri_Overlap_Box.tri_overlap_box;
import static gridanalysis.irreg.Voxel_Map.ENTRY_SHIFT;
import static gridanalysis.irreg.Voxel_Map.entry_begin;
import static gridanalysis.irreg.Voxel_Map.entry_log_dim;
import static gridanalysis.irreg.Voxel_Map.make_entry;
import gridanalysis.utilities.list.ObjectList;
import static java.lang.Math.cbrt;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 *
 * @author user
 */
public class Grid2 {
    /// Counts the number of elements in the union of two sorted arrays.
    public static int count_union(IntegerList p0, int c0, IntegerList p1, int c1) {
        int i = 0;
        int j = 0;

        int count = 0;
        while (i < c0 & j < c1) {
            int k0 = p0.get(i) <= p1.get(j) ? 1 : 0;
            int k1 = p0.get(i) >= p1.get(j) ? 1 : 0;
            i += k0;
            j += k1;
            count++;
        }

        return count + (c1 - j) + (c0 - i);
    }
    
    /// Computes the dimensions of a grid using the formula : R{x, y, z} = e{x, y, z} * (N * d / V)^(1/3).
    public static void compute_grid_dims(Float2 e, int N, float d, int[] dims) {
        float V = e.x * e.y;
        float r = (float) (cbrt(d * N / V));
        dims[0] = Math.max(1, (int)(e.x * r));
        dims[1] = Math.max(1, (int)(e.y * r));
        
        
    }

    
    public static void compute_bboxes(GridInfo info, Tri2[] tris, BBox2[] bboxes) {
        info.bbox = IntStream.range(0, tris.length)                
                .mapToObj(i -> {
                    BBox2 box = tris[i].bbox();
                    bboxes[i] = box;
                    return box;
                }).reduce(new BBox2(), (a, b)-> a.extend(b)); 
    }
    
    /// Computes the range of voxels covered by a bounding box on a grid.
    public static Range2 find_coverage(BBox2 bb, Float2 inv_org, Float2 inv_size, int[] dims) {
        int lx = (int) clamp(bb.min.x * inv_size.x - inv_org.x, 0, dims[0] - 1);
        int ly = (int) clamp(bb.min.y * inv_size.y - inv_org.y, 0, dims[1] - 1);
        
        int hx = (int) clamp(bb.max.x * inv_size.x - inv_org.x, 0, dims[0] - 1);
        int hy = (int) clamp(bb.max.y * inv_size.y - inv_org.y, 0, dims[1] - 1);       

        return new Range2(lx, hx, ly, hy);
    }
    
    public static void gen_top_refs(
                        GridInfo info,
                        BBox2[] bboxes,
                        Tri2[] tris,
                        final ObjectList<Ref> refs) {
        Float2 cell_size = info.cell_size();
        Float2 inv_size  = div(1.0f, cell_size);
        Float2 inv_org   = mul(info.bbox.min, inv_size);

        IntegerList approx_ref_counts = new IntegerList(new int[tris.length + 1]);
        
        IntStream.range(0, tris.length)
                .parallel()
                .forEach(i->{
                    Range2 range = find_coverage(bboxes[i], inv_org, inv_size, info.dims);
                    approx_ref_counts.set(i + 1, range.size());
                });
        
        // Get the insertion position into the array of references
        approx_ref_counts.set(0, 0);
        approx_ref_counts.prefixSum();
             
        // Allocate and fill the array of references
        
        refs.resize(approx_ref_counts.back(), ()-> new Ref(-1, -1 ,-1));         
        IntStream.range(0, tris.length)
                //.parallel()
                .forEach(i->{
                    
                    Range2 cov = find_coverage(bboxes[i], inv_org, inv_size, info.dims);
                    Tri2 tri = tris[i];
                    
                                                           
                    // Examine each cell and determine if the triangle is really inside
                    AtomicInteger index = new AtomicInteger(approx_ref_counts.get(i));     
                    
                    cov.iterate((x, y)->{
                        Float2 cell_min = add(info.bbox.min, mul(cell_size, new Float2(x, y)));
                        Float2 cell_max = add(info.bbox.min, mul(cell_size, new Float2(x + 1, y + 1)));
                        
                        if(Tri_Overlap_Box.tri_overlap_box(true, true, tri.v0, tri.e1, tri.e2, tri.normal(), cell_min, cell_max))
                            refs.set(index.getAndIncrement(), new Ref(i, x + info.dims[0] * y, 0));                        
                    });                    
                });
              
        // Remove the references that were culled by the triangle-box test from the array
        refs.removeIf(ref -> ref.tri < 0);          
    }
    
    public static void compute_snd_dims(GridInfo info, float snd_density, ObjectList<Ref> refs, IntegerList snd_dims)
    {
        final int num_top_cells = info.num_top_cells();
        final Float2 cell_size = info.cell_size();
        
        // Compute the number of references per cell
        ObjectList<AtomicInteger> cell_ref_counts = new ObjectList(num_top_cells, ()-> new AtomicInteger());        
        IntStream.range(0, refs.size())
                .parallel()
                .forEach(i->{                   
                    cell_ref_counts.get(refs.get(i).top_cell).getAndIncrement();
                });
        
        snd_dims.resize(num_top_cells);
               
        // Compute the second level dimensions for each top-level cell
        IntStream.range(0, num_top_cells)
                .forEach(i->{
                    int inner_dims[] = new int[2];
                    compute_grid_dims(cell_size, cell_ref_counts.get(i).get(), snd_density, inner_dims);
                    int max_dim = Math.max(inner_dims[0], inner_dims[1]);
                    snd_dims.set(i, Math.min(closest_log2(max_dim), (1 << ENTRY_SHIFT) - 1));
                });
        info.max_snd_dim = snd_dims.max(); 
    }
    
    public static Float2 compute_cell_pos(long snd_cell, Float2 cell_size) {
        Float2 cur_size = cell_size;
        Float2 pos = new Float2(0.0f);
        while (snd_cell > 0) {            
            pos.x += (snd_cell & 1) != 0 ? cur_size.x : 0.0f;
            pos.y += (snd_cell & 2) != 0 ? cur_size.y : 0.0f;
            cur_size.mulAssign(2f);
            snd_cell >>= 2;  
        }
        return pos;
    }
        
    public static int find(IntegerList list, int first, int end, int value)
    {
        int flags_it = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == value) {
                flags_it = i;
                break;
            }
        }
        return flags_it;
    }
    
    public static void remove_invalid_references(IntegerList flags, ObjectList<Ref> refs, int first_ref) {              
        int flags_it = find(flags, 0, flags.end(), 0);
        if (flags_it > 0 && flags_it != flags.end()) {
            
            int refs_it = first_ref + flags_it;            
            int it1 = refs_it  + 1;
            int it2 = flags_it + 1;
            for (; it1 != refs.size(); ++it1, ++it2) {
                if (flags.get(it2) != 0) {
                    flags.set(flags_it++, flags.get(it2));
                    refs.set(refs_it++, refs.get(it1));                   
                }
            }
            
            flags.remove(flags_it, flags.size()); 
            refs.remove(refs_it, refs.size());
        }
    }
    
    public static BBox2 compute_cell_box(int[] dims, int top_cell, long snd_cell, Float2 org, Float2 cell_size, int iter) {
        int x = top_cell % dims[0];
        int y = (top_cell / dims[0]) % dims[1];       
        Float2 sub_cell_size = mul(cell_size, (1.0f / (1 << iter)));
        Float2 pos = addAll(compute_cell_pos(snd_cell, sub_cell_size), mul(cell_size, new Float2(x, y)), org);
        return new BBox2(pos, add(pos, sub_cell_size));
    }
    
    public static void subdivide_refs(GridInfo info, Tri2[] tris, IntegerList snd_dims, ObjectList<Ref> refs) {
        Float2 cell_size = info.cell_size();

        AtomicInteger first_ref = new AtomicInteger(0);
        IntegerList split_flags = new IntegerList();
        IntegerList split_counts = new IntegerList();        
        ObjectList<Ref> new_refs = new ObjectList();

        // Subdivide until the maximum depth is reached
        AtomicInteger iter = new AtomicInteger(0);
        
        while (iter.get() < info.max_snd_dim) {
            // Partition the set of references so that the ones that will not be subdivided anymore are in front            
            first_ref.set(refs.partition((Ref ref)-> snd_dims.get(ref.top_cell) <= iter.get()));
            
            int valid_refs = refs.size() - first_ref.get();
            if (valid_refs == 0) break;
                        
            // Compute, for each reference, how many sub-references will be created (up to 8 by reference)
            split_flags.resize(valid_refs);
            IntStream.range(0, valid_refs)
                    .forEach(i -> {
                        Ref ref = refs.get(first_ref.get() + i);
                        
                        BBox2 cell_box = compute_cell_box(info.dims, ref.top_cell, ref.snd_cell, info.bbox.min, cell_size, iter.get());
                        Float2 center = mul(add(cell_box.max, cell_box.min), 0.5f);
                        
                        Tri2 tri = tris[ref.tri];
                        int flag = 0;
                        
                        for (int j = 0; j < 4; j++) {
                            Float2 min = new Float2(
                                    (j & 1) != 0 ? center.x : cell_box.min.x,
                                    (j & 2) != 0 ? center.y : cell_box.min.y);
                            Float2 max = new Float2(
                                    (j & 1) != 0 ? cell_box.max.x : center.x,
                                    (j & 2) != 0 ? cell_box.max.y : center.y);
                            
                            if (tri_overlap_box(true, true, tri.v0, tri.e1, tri.e2, tri.normal(), min, max)) 
                                flag |= 1 << j;
                            
                            // The result is a bitfield whose popcount is the number of sub-refs for this reference
                            split_flags.set(i, flag);                            
                        }
                    });
            
            // Sometimes, a reference that is in a cell is in no sub-cell of that cell,
            // because of precision problems. We remove those problematic references here.            
            remove_invalid_references(split_flags, refs, first_ref.get());
            
            split_counts.resize(valid_refs + 1);
            
            IntStream.range(0, valid_refs)
                    .forEach(i->{
                        int pop_count[] = { 0, 1, 1, 2,
                                            1, 2, 2, 3,
                                            1, 2, 2, 3,
                                            2, 3, 3, 4};
                        
                        int flag = split_flags.get(i);
                        // Could use the popcnt x86 instruction here or a similar one for other
                        // architectures (but we only need 4 bits, and portability is a bigger issue)
                        split_counts.set(i + 1, pop_count[flag]);  
            });
            
            // Sum the number of primitives split in order to know their insertion point in the array
            split_counts.set(0, first_ref.get());
            split_counts.prefixSum();
                                    
            // Allocate the new references
            new_refs.clear();
            for(int i = 0; i<split_counts.back(); i++)
                new_refs.add(new Ref());
            
            // Copy the references that will not be split into the new array
            for(int i = 0; i<first_ref.get(); i++)
                new_refs.set(i, refs.get(i));
            
            IntStream.range(0, valid_refs)
                    .forEach(i->{
                        Ref ref = refs.get(first_ref.get() + i);
                        int flag  = split_flags.get(i);
                        int index = split_counts.get(i);
                        
                        for (int j = 0; j < 4; j++) {
                            if ((flag & (1 << j)) != 0) {
                                // Add one reference
                                long snd_cell = (ref.snd_cell << 2) | j;
                                new_refs.set(index++, new Ref(ref.tri, ref.top_cell, snd_cell));
                            }
                        }

                    });
            
            refs.swap(new_refs);
            iter.getAndIncrement();     
        }        
    }
    
    public static int compute_empty_cells(long a, long b, boolean empty) {
        // Compute the number of empty cells between the octree cell a and octree cell b
        int count = 0;
        while (a != b) {
            final long ka = a & 3;
            final long kb = b & 3;

            count += (a & ~3) == (b & ~3) ? kb - ka - (empty ? 0 : 1) : (empty ? 0 : 3 - ka) + kb;

            a >>= 2;
            b >>= 2;
        }
        return count;
    }
    
    public static void compact_references(
                               ObjectList<Ref> refs,
                               ObjectList<Ref> compact_refs, 
                               IntegerList cell_begins,
                               IntegerList cell_ends) {
        int cur = 0;
        
        compact_refs.resize(refs.size());   
        cell_begins.resize(refs.size());
        cell_ends.resize(refs.size());
        
        // Perform a run-length encoding of the references
        for (int i = 0; i < refs.size(); i++) {
            Ref prev = i > 0 ? refs.get(i - 1) : new Ref(-1, -1, -1);
            Ref ref = refs.get(i);

            if (ref.top_cell == prev.top_cell &&
                ref.snd_cell == prev.snd_cell) {   
                cell_ends.increment(cur - 1);
            } else {
                compact_refs.set(cur, ref);
                cell_begins.set(cur, i);
                cell_ends.set(cur, i + 1);
                cur++;
            }
        }
        refs.resize(cur);
        cell_begins.resize(cur);
        cell_ends.resize(cur);
    }
    
    public static void compute_cell_pos_int(int cur_size, long snd_cell, int[] pos) {
        // Compute the position of the cell on the grid in integer coordinates
        while (snd_cell > 0) {
            pos[0] += (snd_cell & 1) != 0 ? cur_size : 0;
            pos[1] += (snd_cell & 2) != 0 ? cur_size : 0;
            cur_size <<= 1;
            snd_cell >>= 2;
        }
    }
    
    public static int generate_cells(
                        GridInfo info,
                        int snd_dim, int top_cell,
                        ObjectList<Cell2> cells, int begin, int end,
                        long a, long b) {
        // This function generates a cell with with morton code 'a' and primitive range [begin, end],
        // and then proceeds to generate the empty cells between this cell and the cell with morton code 'b'.
        // If the range [begin, end] is empty, then the cell 'a' is treated as an empty cell, and the
        // algorithm will generate the biggest possible cell with this morton code.
        
        int x = top_cell % info.dims[0];
        int y = (top_cell / info.dims[0]) % info.dims[1];        
        int org_x = (1 << info.max_snd_dim) * x;
        int org_y = (1 << info.max_snd_dim) * y;
        
        int cell_size = 1 << (info.max_snd_dim - snd_dim);
        int cur_size = cell_size;
        int num_cells = 0;
        
        int offset_a[] = new int[]{org_x, org_y};
        int offset_b[] = new int[]{org_x, org_y};
        compute_cell_pos_int(cell_size, a, offset_a);
        compute_cell_pos_int(cell_size, b, offset_b);
        
        boolean empty = begin >= end;
        if (!empty) {
            Cell2 cell = cells.get(num_cells++);
            cell.min[0] = offset_a[0];
            cell.min[1] = offset_a[1];

            cell.max[0] = offset_a[0] + cell_size;
            cell.max[1] = offset_a[1] + cell_size;

            cell.begin = begin;
            cell.end   = end;
        }
        
        // Traverse the tree from bottom to top, starting from both leaves, until a common ancestor is found
        while (a != b) {
            long ka = a & 3; //similar to a%3 which is a remainder after division by 4
            long kb = b & 3;

            offset_a[0] -= (ka & 1) !=0 ? cur_size : 0;
            offset_a[1] -= (ka & 2) !=0 ? cur_size : 0;

            offset_b[0] -= (kb & 1) !=0 ? cur_size : 0;
            offset_b[1] -= (kb & 2) !=0 ? cur_size : 0;

            boolean same_ancestor = (a & ~3) == (b & ~3); //to the nearest number divisible by 4
            long n1 = same_ancestor ? kb : 4;
            long n2 = same_ancestor ? kb : 0;

            // If the left cell is empty, no need to generate cells on the left
            if (!empty || same_ancestor) {
                // Generate n1 - 1 cells in a's subtree, or n1 if the left subtree is empty
                for (long i = ka + (empty ? 0 : 1); i < n1; i++) {
                    Cell2 cell = cells.get(num_cells++);
                    cell.min[0] = offset_a[0] + ((i & 1) != 0 ? cur_size : 0);
                    cell.min[1] = offset_a[1] + ((i & 2) != 0 ? cur_size : 0);
                    
                    cell.max[0] = cell.min[0] + cur_size;
                    cell.max[1] = cell.min[1] + cur_size;
                    
                    cell.begin = 0;
                    cell.end   = 0;
                }
            }

            // Generate n2 cells in b's subtree
            for (long i = n2; i < kb; i++) {
                Cell2 cell = cells.get(num_cells++);
                cell.min[0] = offset_b[0] + ((i & 1) != 0 ? cur_size : 0);
                cell.min[1] = offset_b[1] + ((i & 2) != 0 ? cur_size : 0);

                cell.max[0] = cell.min[0] + cur_size;
                cell.max[1] = cell.min[1] + cur_size;

                cell.begin = 0;
                cell.end   = 0;
            }

            cur_size <<= 1;
            a >>= 2; //divide by 4
            b >>= 2;
        }

        return num_cells;
    }
    public static void gen_cells(GridInfo info, ObjectList<Ref> refs, IntegerList snd_dims, ObjectList<Cell2> cells) {
        // Sort by cell first, then by morton code index
        refs.sort((Ref a, Ref b)->{
            boolean cond = 
                    a.top_cell < b.top_cell || (a.top_cell == b.top_cell && (
                    a.snd_cell < b.snd_cell || (a.snd_cell == b.snd_cell &&
                    a.tri < b.tri)));
            
            return cond ? -1 : 1;
        });
        
        // Compact the references in order to keep only one reference per (non-empty) cell.
        // At the same time, compute the range of references covered by these cells.
        ObjectList<Ref> compact_refs = new ObjectList();
        IntegerList cell_begins = new IntegerList();
        IntegerList cell_ends = new IntegerList();
        
        compact_references(refs, compact_refs, cell_begins, cell_ends);
        
        // Allocate an array to hold the number of cells per top-level cell, and the number of refs per top-level cell
        int num_top_cells = info.num_top_cells(); 
        ObjectList<AtomicInteger> num_cells = new ObjectList(compact_refs.size() + 1, ()-> new AtomicInteger(0));        
        IntegerList empty_cells = new IntegerList(); empty_cells.resize(num_top_cells + 1, 1);        
        
        IntStream.range(0, compact_refs.size())
                .forEach(i->{
                    Ref prev = i > 0 ? compact_refs.get(i - 1) : new Ref(-1, -1, -1);
                    Ref next = i < compact_refs.size() - 1 ? compact_refs.get(i + 1) : new Ref(-1, -1, -1);
                    Ref cur  = compact_refs.get(i);

                    AtomicInteger num = num_cells.get(i + 1);
                    
                    int prev_cells = 0;
                    if (cur.top_cell != prev.top_cell) {
                        // The previous reference belongs to a different top-level cell
                        prev_cells = compute_empty_cells(0, cur.snd_cell, true);                       
                    }
                    
                    final long next_snd_cell = cur.top_cell != next.top_cell ? 1 << (2 * snd_dims.get(cur.top_cell)) : next.snd_cell;
                    num.addAndGet(prev_cells + 1 + compute_empty_cells(cur.snd_cell, next_snd_cell, false));                    
                    empty_cells.set(cur.top_cell + 1, 0);
                });
        
        
               
        // Get the insertion position for each compacted reference
        num_cells.prefix((AtomicInteger a, AtomicInteger b)->{
            return new AtomicInteger(a.get() + b.get());
        });
                
        // Perform a scan to count empty cells
        empty_cells.set(0, 0);
        empty_cells.prefixSum();
                
        AtomicInteger num_non_empty = num_cells.get(num_cells.size()-1);
        int num_top_empty = empty_cells.back(); 
        cells.resize(num_non_empty.get() + num_top_empty, ()-> new Cell2());
                
        IntStream.range(0, compact_refs.size())
                .forEach(i->{
                    AtomicInteger num   = num_cells.get(i);
                    Ref cur             = compact_refs.get(i);
                    Ref prev            = i > 0 ? compact_refs.get(i - 1) : new Ref(-1, -1, -1);
                    Ref next            = i < compact_refs.size() - 1 ? compact_refs.get(i + 1) : new Ref(-1, -1, -1);
                    int begin           = cell_begins.get(i);
                    int end             = cell_ends.get(i);
                    int snd_dim         = snd_dims.get(cur.top_cell);
                    
                    int prev_cells = 0;
                    if (cur.top_cell != prev.top_cell) {
                        // Generate empty cells before this one if it is the beginning of a top-level cell
                        prev_cells = generate_cells(info, snd_dim, cur.top_cell, cells.getSublistFrom(num.get()), 0, 0, 0, cur.snd_cell);
                    }
                    
                    long next_snd_cell = cur.top_cell != next.top_cell ? 1 << (2 * snd_dims.get(cur.top_cell)) : next.snd_cell;                    
                    int total = prev_cells + generate_cells(info, snd_dim, cur.top_cell, cells.getSublistFrom(num.get() + prev_cells), begin, end, cur.snd_cell, next_snd_cell);
                    
                });
                
        IntStream.range(0, num_top_cells)
                .forEach(i->{
                    int index = empty_cells.get(i);
                    if (index != empty_cells.get(i + 1)) {
                        int x = i % info.dims[0];
                        int y = i / info.dims[0];
                        
                        int d = 1 << info.max_snd_dim;
                        int org_x = d * x;
                        int org_y = d * y;
                        
                        Cell2 cell = cells.get(num_non_empty.get() + index);
                        
                        cell.min[0] = org_x;
                        cell.min[1] = org_y;
                        
                        cell.max[0] = org_x + d;
                        cell.max[1] = org_y + d;
                        
                        cell.begin = 0;
                        cell.end   = 0;              
                    }                    
                });
    }
    
    public static void fill_cell_entries(GridInfo info, Cell2 cell, int id, IntegerList entries) {
        // Find top-level cell
        int top_x = cell.min[0] >> info.max_snd_dim;
        int top_y = cell.min[1] >> info.max_snd_dim;
        int entry   = entries.get(top_x + info.dims[0] * top_y);
        int begin   = entry_begin(entry);
        int log_dim = entry_log_dim(entry);
        int mask    = (1 << info.max_snd_dim) - 1;
        int d       = 1 << (info.max_snd_dim - log_dim);
        
        for (int y = cell.min[1]; y < cell.max[1]; y += d) {
            for (int x = cell.min[0]; x < cell.max[0]; x += d) {
                // Compute offset within top-level cell
                int snd_x = (x & mask) >> (info.max_snd_dim - log_dim);
                int snd_y = (y & mask) >> (info.max_snd_dim - log_dim);
                
                int k = snd_x + (snd_y << log_dim);
                
                entries.set(begin + k, id);
            }
        }
    }
    
    public static void gen_entries(GridInfo info, ObjectList<Cell2> cells, IntegerList snd_dims, IntegerList entries) {
        // Reminder: snd_dims.size() == number of top-level cells
        int num_top_cells = snd_dims.size(); 
        
        IntegerList accum_dims = new IntegerList(new int[num_top_cells + 1]);
        for (int i = 0; i < num_top_cells; i++) {
            int d = 1 << snd_dims.get(i);           
            accum_dims.set(i + 1, d * d);
        }
        
        accum_dims.set(0, num_top_cells);
        accum_dims.prefixSum();
        
        // Allocate (number of top-level cells) + (accumulated number of second-level cells) entries for the voxel map
        entries.resize(accum_dims.back(), -1);
        
        
        // Generate the top-level entries
        IntStream.range(0, num_top_cells)
                .forEach(i->{
                    entries.set(i, make_entry(accum_dims.get(i), snd_dims.get(i)));
                });
        
        // Generate the second-level entries
        IntStream.range(0, cells.size())
                .forEach(i->{
                    fill_cell_entries(info, cells.get(i), i, entries);
                });
        
       
    }
    
    public static void transform_cells(GridInfo info, ObjectList<Cell2> cells) {
        Float2 subcell_size = div(info.cell_size(), (1 << info.max_snd_dim));
                
        IntStream.range(0, cells.size())
                .parallel()
                .forEach(i->{
                    Cell2 cell = cells.get(i);

                    Float2 min = add(info.bbox.min, mul(subcell_size, new Float2(cell.min[0], cell.min[1])));
                    Float2 max = add(info.bbox.min, mul(subcell_size, new Float2(cell.max[0], cell.max[1])));
                    
                    cell.min[0] = Float.floatToIntBits(min.x);
                    cell.min[1] = Float.floatToIntBits(min.y);

                    cell.max[0] = Float.floatToIntBits(max.x);
                    cell.max[1] = Float.floatToIntBits(max.y); 
                });
    }
}
