/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import java.util.ArrayList;
import java.util.Comparator;
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
                    ArrayList<MergePair> pairs,
                    ArrayList<Cell2> cells,
                    IntList refs,
                    IntList entries) {
        if (pairs.isEmpty()) return;
        
        pairs.sort(Comparator.<MergePair>comparingInt(a -> a.first).thenComparing(a->a.second));
        
        // Build new references
        IntList new_refs = new IntList();
        IntList indices = new IntList(cells.size());
        int cur_id = 0, p = 0;
        
        for (int i = 0; i < cells.size(); i++) {
            if (p < pairs.size() && pairs.get(p).first == i) {
                int e0 = i;
                int e1 = pairs.get(p).second;
                p++;

                if (e1 < i) continue;

                cells.get(cur_id).min[0] = Math.min(cells.get(e0).min[0], cells.get(e1).min[0]);
                cells.get(cur_id).min[1] = Math.min(cells.get(e0).min[1], cells.get(e1).min[1]);
                cells.get(cur_id).min[2] = Math.min(cells.get(e0).min[2], cells.get(e1).min[2]);
                cells.get(cur_id).max[0] = Math.max(cells.get(e0).max[0], cells.get(e1).max[0]);
                cells.get(cur_id).max[1] = Math.max(cells.get(e0).max[1], cells.get(e1).max[1]);
                cells.get(cur_id).max[2] = Math.max(cells.get(e0).max[2], cells.get(e1).max[2]);
                
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
                cells.get(cur_id).min[2] = cells.get(i).min[2];
                cells.get(cur_id).max[0] = cells.get(i).max[0];
                cells.get(cur_id).max[1] = cells.get(i).max[1];
                cells.get(cur_id).max[2] = cells.get(i).max[2];

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
}
