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
    
    /// Collapses sub-entries that map to the same cell/sub-sub-entry
    public void collapse_entries(Entry[] entries, int first, int num_entries) {
        for(int id = 0; id < num_entries; id++)
        {            
            if (id >= num_entries) return;

            Entry entry = entries[first + id];
            if (entry.log_dim != 0) {
                Vec2i ptr0 = entries[entry.begin].asVec2i();
                
                if (ptr0.x == ptr0.y) {
                    Vec2i ptr1 = entries[entry.begin + 2].asVec2i();
                    if (ptr0.x == ptr1.x &&
                        ptr1.x == ptr1.y) {
                        entries[first + id] = new Entry(ptr0.x, ptr1.y);
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
