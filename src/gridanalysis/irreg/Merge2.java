/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import gridanalysis.utilities.list.IntegerList;
import static gridanalysis.irreg.Voxel_Map.lookup_entry;
import gridanalysis.utilities.list.ObjectList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 *
 * @author user
 */
public class Merge2 {
    public static boolean restricted_merge(int x, int shift, int iter) {
        // Make sure top-level cells are aligned so that
        // they can be merged along another dimension
        int top_mask = (1 << shift) - 1;
        int empty_mask = iter > 3 ? 0 : (1 << (iter + 1)) - 1;
        return !((x & top_mask) != 0) && (((x >> shift) & empty_mask)!= 0);
    }
    
    public static void merge_pairs(
                    int top_entries,
                    ObjectList<MergePair> pairs,
                    ObjectList<Cell2> cells,
                    IntegerList refs,
                    IntegerList entries) {
        if (pairs.isEmpty()) return;
        
        pairs.sort(Comparator.<MergePair>comparingInt(a -> a.first).thenComparing(a->a.second));
        
        // Build new references
        IntegerList new_refs = new IntegerList();
        IntegerList indices = new IntegerList(new int[cells.size()]);
        
        int cur_id = 0, p = 0;
        
        for (int i = 0; i < cells.size(); i++) {
            if (p < pairs.size() && pairs.get(p).first == i) {
                int e0 = i;
                int e1 = pairs.get(p).second;
                p++;

                if (e1 < i) continue;
                
                //System.out.println(cells.size()+ " " +e1);
                
                cells.get(cur_id).min[0] = Math.min(cells.get(e0).min[0], cells.get(e1).min[0]);
                cells.get(cur_id).min[1] = Math.min(cells.get(e0).min[1], cells.get(e1).min[1]);
                cells.get(cur_id).max[0] = Math.max(cells.get(e0).max[0], cells.get(e1).max[0]);
                cells.get(cur_id).max[1] = Math.max(cells.get(e0).max[1], cells.get(e1).max[1]);
                
                // Merge references
                int begin = new_refs.size();
                int a = cells.get(e0).begin;
                int b = cells.get(e1).begin;
                
                while (a < cells.get(e0).end && b < cells.get(e1).end) {
                    if (refs.get(a) < refs.get(b)) {
                        new_refs.add(refs.get(a));
                        a++;
                    } else if (refs.get(a) > refs.get(b)) {
                        new_refs.add(refs.get(b));
                        b++;
                    } else {
                        new_refs.add(refs.get(a));
                        a++; b++;
                    }
                }
                
                for (int j = a; j < cells.get(e0).end; j++) new_refs.add(refs.get(j));
                for (int j = b; j < cells.get(e1).end; j++) new_refs.add(refs.get(j));

                cells.get(cur_id).begin = begin;
                cells.get(cur_id).end = new_refs.size();
                indices.set(e1, cur_id);
            } 
            else {
                int begin = new_refs.size();
                for (int j = cells.get(i).begin; j < cells.get(i).end; j++)
                    new_refs.add(refs.get(j));

                cells.get(cur_id).min[0] = cells.get(i).min[0];
                cells.get(cur_id).min[1] = cells.get(i).min[1];
                cells.get(cur_id).max[0] = cells.get(i).max[0];
                cells.get(cur_id).max[1] = cells.get(i).max[1];

                cells.get(cur_id).begin = begin;
                cells.get(cur_id).end = new_refs.size();
            }
            
            indices.set(i, cur_id++);
        }
        refs.swap(new_refs);
        cells.subList(cur_id, cells.size()).clear(); //resize
        
        
        IntStream.range(top_entries, entries.size())
                .parallel()
                .forEach(i -> {
                    entries.set(i, indices.get(entries.get(i)));
                });
    }
    
    public static int merge(
            int iter,
            GridInfo info,
            ObjectList<Cell2> cells,
            IntegerList refs,
            IntegerList entries) {
        Float2 cell_size = info.cell_size();
        int top_entries = info.num_top_cells();

        int dims[] = {
            info.dims[0] << info.max_snd_dim,
            info.dims[1] << info.max_snd_dim};
        
        ObjectList<AtomicBoolean> merge_flag = new ObjectList(cells.size(), ()-> new AtomicBoolean(false));        
        ObjectList<MergePair> to_merge = new ObjectList();
        int total_merged = 0;
        
        // Try to merge on x-axis
        IntStream.range(0, cells.size())
                .forEach(i->{
                    Cell2 cell0 = cells.get(i);

                    final int x1 = cell0.max[0];
                    final int y1 = cell0.min[1];
                    if (x1 >= dims[0] || restricted_merge(cell0.min[0], info.max_snd_dim, iter)) return;
                    
                    int entry = lookup_entry(entries, info.dims, info.max_snd_dim, x1, y1);
                    Cell2 cell1 = cells.get(entry);
                    
                    if (cell0.can_merge(0, cell1) && cell0.is_merge_profitable(cell_size, refs, cell1)) {
                        //System.out.println(entry);
                        if (merge_flag.get(entry).compareAndSet(false, true)) {
                            if (merge_flag.get(i).compareAndSet(false, true)) {                                
                                to_merge.add(new MergePair(i, entry));
                                to_merge.add(new MergePair(entry, i));
                            }else{
                                merge_flag.get(entry).set(true);   
                            }
                        }
                    }
                });
        
        total_merged += to_merge.size() / 2; 
        merge_pairs(top_entries, to_merge, cells, refs, entries);
        
        //std::fill(merge_flag.begin(), merge_flag.end(), true);
        to_merge.clear();
        Common.clearFill(merge_flag, merge_flag.size(), ()-> new AtomicBoolean(false));
        
        // Try to merge on y-axis
        IntStream.range(0, cells.size())
                .forEach(i->{
                    Cell2 cell0 = cells.get(i);

                    int x1 = cell0.min[0];
                    int y1 = cell0.max[1];
                    if (y1 >= dims[1] || restricted_merge(cell0.min[1], info.max_snd_dim, iter)) return;
                    
                    int entry = lookup_entry(entries, info.dims, info.max_snd_dim, x1, y1);
                    Cell2 cell1 = cells.get(entry);
                    
                    if (cell0.can_merge(1, cell1) && cell0.is_merge_profitable(cell_size, refs, cell1)) {
                        if (merge_flag.get(entry).compareAndSet(false, true)) {
                            if (merge_flag.get(i).compareAndSet(false, true)) {
                                to_merge.add(new MergePair(i, entry));
                                to_merge.add(new MergePair(entry, i));
                            }else{
                                merge_flag.get(entry).set(true);   
                            }
                        }
                    }
                });
        
        total_merged += to_merge.size() / 2;
        merge_pairs(top_entries, to_merge, cells, refs, entries);
        return total_merged;
    }
}
