/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec4i;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.jfx.MEngine;
import gridanalysis.jfx.shape.MCellInfo;
import gridanalysis.utilities.BitUtility;
import gridanalysis.utilities.list.IntegerList;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 *
 * @author user
 */
public class Flatten extends GridAbstracts{
    private final MEngine engine;
    
    int flat_levels = (1 << Entry.LOG_DIM_BITS) - 1;
    
    public Flatten(MEngine engine)
    {
        this.engine = engine;
    }
    
    private Vec4i getVec4i(Entry e1, Entry e2, Entry e3, Entry e4)
    {
        int value1 = 0;
        value1 = BitUtility.apply_bits_at(0, e1.log_dim, value1);
        value1 = BitUtility.apply_bits_at(2, e1.begin, value1);
        
        int value2 = 0;
        value2 = BitUtility.apply_bits_at(0, e2.log_dim, value2);
        value2 = BitUtility.apply_bits_at(2, e2.begin, value2);
        
        int value3 = 0;
        value3 = BitUtility.apply_bits_at(0, e3.log_dim, value3);
        value3 = BitUtility.apply_bits_at(2, e3.begin, value3);
        
        int value4 = 0;
        value4 = BitUtility.apply_bits_at(0, e4.log_dim, value4);
        value4 = BitUtility.apply_bits_at(2, e4.begin, value4);
        
        return new Vec4i(value1, value2, value3, value4);
    }
    
    public Entry asEntry(Vec4i ptr) {
        Entry entry     = new Entry();
        entry.log_dim   = BitUtility.get_bits_at(0, ptr.x, 2);
        entry.begin     = BitUtility.get_bits_at(2, ptr.x, 30);
        return entry;
    }
    
    /// Collapses sub-entries that map to the same cell/sub-sub-entry (Thanks ChatGPT)
    public void collapse_entries(Entry[] entries, int first, int num_entries) {       
        for(int id = 0; id < num_entries; id++)
        {            
            if (id >= num_entries) return;

            Entry entry = entries[first + id];
            if (entry.log_dim != 0) {
                Vec4i ptr = getVec4i(entries[entry.begin], entries[entry.begin + 1], entries[entry.begin + 2], entries[entry.begin + 3]);
                if (ptr.x == ptr.y &&
                    ptr.x == ptr.z &&
                    ptr.x == ptr.w) {                    
                    entries[first + id] = asEntry(ptr);
                    
                }
            }
        }   
    }
    
    /// Computes the depth of each entry
    public void compute_depths(Entry[] entries, IntegerList depths, int first, int num_entries) {
        for(int id = 0; id < num_entries; id++)
        {            
            if (id >= num_entries) return;

            Entry entry = entries[first + id];
            int d = 0;
            if (entry.log_dim != 0) {
                Vec4i ptr = new Vec4i(depths.get(entry.begin), depths.get(entry.begin + 1), depths.get(entry.begin + 2), depths.get(entry.begin + 3));                
                Vec4i d0 = ptr.copy();
                d = 1 + max(max(d0.x, d0.y),
                            max(d0.z, d0.w));
            }
            depths.set(first + id, d);
        }
    }
    
    /// Copies the top-level entries and change their depth & start index
    public void copy_top_level( Entry[] entries,
                                IntegerList start_entries,
                                IntegerList depths,
                                Entry[] new_entries,
                                int num_entries) {
        for(int id = 0; id < num_entries; id++)
        {            
            if (id >= num_entries) return;

            Entry entry = entries[id];
            if (entry.log_dim != 0) {
                entry = make_entry(min(depths.get(id), flat_levels), num_entries + start_entries.get(id));
            }
            new_entries[id] = entry;
        }
    }
    
    /// Flattens several voxel map levels into one larger level
    public void flatten_level(  Entry[] entries,
                                IntegerList start_entries,
                                IntegerList depths,
                                Entry[] new_entries,
                                int first_entry,
                                int offset, int next_offset,
                                int num_entries) {
        for(int id = 0; id < num_entries; id++)
        {
            if (id >= num_entries) return;
            
            int d = min(depths.get(id + first_entry), flat_levels); 
            
            int num_sub_entries = d == 0 ? 0 : 1 << (2 * d); 
            if (num_sub_entries <= 0) continue;

            int start = offset + start_entries.get(id + first_entry);
            Entry root = entries[id + first_entry];
            
            for (int i = 0; i < num_sub_entries; i++) {
                
                // Treat i as a morton code
                int cur_d = d;
                int x = 0, y = 0;
                int next_id = id;
                Entry entry = root.copy();
                while (cur_d > 0) {
                    cur_d--;

                    int pos = i >> (cur_d * 2);
                    x += (pos & 1) != 0 ? (1 << cur_d) : 0;
                    y += (pos & 2) != 0 ? (1 << cur_d) : 0;

                    if (entry.log_dim != 0) {
                        next_id = entry.begin + (pos & 3);
                        entry = entries[next_id];
                    }
                }
                
                if (entry.log_dim != 0) {
                    entry = make_entry(min(depths.get(next_id), flat_levels), next_offset + start_entries.get(next_id));
                }                
                new_entries[start + x + (y << d)] = entry;
            }
        }
    }
    
    public void flatten_grid(Grid grid) {
        IntegerList depths = new IntegerList(new int[grid.num_entries + 1]);  
        // Flatten the voxel map
        for (int i = grid.shift; i >= 0; i--) {
            int first = i > 0 ? grid.offsets.get(i - 1) : 0;
            int last  = grid.offsets.get(i);
            int num_entries = last - first; //num of entries in this level
            
            // Collapse voxel map entries when possible
            collapse_entries(grid.entries, first, num_entries);
            compute_depths(grid.entries, depths, first, num_entries);
        }
        
        // Compute the insertion position of each flattened level, and the total new number of entries
        IntegerList start_entries = new IntegerList(new int[grid.num_entries + 1]);
        IntegerList level_offsets = new IntegerList(new int[grid.shift]);        
        int total_entries = grid.offsets.get(0);
        
        for (int i = 0; i < grid.shift; i += flat_levels) {
            int first = i > 0 ? grid.offsets.get(i - 1) : 0;
            int last  = grid.offsets.get(i);
            int num_entries = last - first;
            
            depths.getSubList(first, last + 1).transform(0, num_entries + 1, start_entries.getSubList(first, last + 1), d -> d > 0 ? 1 << (min(d, flat_levels) * 2) : 0);
            start_entries.getSubList(first, last + 1).shiftRight(1);
            
            int num_new_entries = start_entries.getSubList(first, last + 1).prefixSum();
            
            level_offsets.set(i, total_entries);
            total_entries += num_new_entries;           
        }
                        
        // Flatten the voxel map, by concatenating consecutive several levels together
        Entry[] new_entries = new Entry[total_entries];        
        IntegerList new_offsets = new IntegerList();
        
        System.out.println(grid.entries.length);
        System.out.println(new_entries.length);
        
        copy_top_level(grid.entries, start_entries, depths, new_entries, grid.offsets.get(0));
        
        
        for (int i = 0; i < grid.shift; i += flat_levels) {
            int first = i > 0 ? grid.offsets.get(i - 1) : 0;
            int last  = grid.offsets.get(i);
            int num_entries = last - first;
                        
            int next_offset = i + flat_levels < grid.shift ? level_offsets.get(i + flat_levels) : 0;
            
            flatten_level(  grid.entries,
                            start_entries,
                            depths,
                            new_entries,
                            first,
                            level_offsets.get(i),
                            next_offset,
                            num_entries);
            
            new_offsets.add(level_offsets.get(i));
        }
        
        new_offsets.add(total_entries);
                
        grid.entries = new_entries; //std::swap(new_entries, grid.entries);
        grid.offsets = new_offsets; //std::swap(new_offsets, grid.offsets);
        
        grid.num_entries = total_entries;
        
       
        engine.setMCellInfo(MCellInfo.getCells(engine, grid, grid.bbox, grid.dims, grid.shift));
    }
    
}
