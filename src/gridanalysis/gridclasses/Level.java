/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

import gridanalysis.utilities.list.IntegerList;


/**
 *
 * @author user
 */
public class Level {
    public IntegerList ref_ids;               ///< Array of primitive indices
    public IntegerList cell_ids;              ///< Array of cell indices
    public int      num_refs;               ///< Number of references in the level
    public int      num_kept;               ///< Number of references kept (remaining is split)
    public Cell[]   cells;                ///< Array of cells
    public Entry[]  entries;             ///< Array of voxel map entries
    public int      num_cells;              ///< Number of cells
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Level ").append("\n");
        builder.append("  num of references: ").append(num_refs).append("\n");
        builder.append("  num of cells:      ").append(num_cells).append("\n");
        if(entries != null)
            builder.append("  num of entries:    ").append(entries.length).append("\n");
        
        return builder.toString();
    }
}
