/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.coordinates.Vec4i;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.jfx.MEngine;
import gridanalysis.utilities.list.IntegerList;

/**
 *
 * @author user
 */
public class Flatten {
    private final MEngine engine;
    
    public Flatten(MEngine engine)
    {
        this.engine = engine;
    }
    
    private Vec4i getVec4i(Entry e1)
    {
        return new Vec4i(e1.log_dim, e1.begin);
    }
    
    public Entry asEntry(Vec4i ptr) {
        Entry entry     = new Entry();
        entry.log_dim   = ptr.x;
        entry.begin     = ptr.y;
        return entry;
    }
    
    /// Collapses sub-entries that map to the same cell/sub-sub-entry (Thanks ChatGPT)
    public void collapse_entries(Entry[] entries, int first, int num_entries) {
        for(int id = 0; id < num_entries; id++)
        {            
            if (id >= num_entries) return;

            Entry entry = entries[first + id];
            if (entry.log_dim != 0) {
                Vec4i[] ptr = new Vec4i[] {getVec4i(entries[entry.begin]), getVec4i(entries[entry.begin + 1])};
                Vec4i ptr0 = ptr[0];
                if (ptr0.x == ptr0.y && ptr0.x == ptr0.z && ptr0.x == ptr0.w) {
                    Vec4i ptr1 = ptr[1];
                    if (ptr0.x == ptr1.x && ptr1.x == ptr1.y && ptr1.x == ptr1.z && ptr1.x == ptr1.w) {
                        entries[first + id] = asEntry(ptr0);
                    }
                }                
            }
        }
    }
    
    public void flatten_grid(Grid grid) {
        IntegerList depths = new IntegerList(new int[grid.num_entries + 1]);
        
        // Flatten the voxel map
        for (int i = grid.shift; i >= 0; i--) {
            int first = i > 0 ? grid.offsets.get(i - 1) : 0;
            int last  = grid.offsets.get(i);
            int num_entries = last - first;
            // Collapse voxel map entries when possible
            collapse_entries(grid.entries, first, num_entries);
        }
    }
}
