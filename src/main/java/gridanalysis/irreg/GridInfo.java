/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import static gridanalysis.irreg.Float2.div;
import static gridanalysis.irreg.Float2.sub;

/**
 * 
 * info.dims                          = total number of cells in top level
 * info.max_snd_dim                   = max depth of octree or maximum second level
 * info.dims << info.max_snd_dim      = total number of sub cells in grid for each dimension (smallest cells)
 * 1 << info.max_snd_dim              = theoretical max/total number of subcells of top cell in each dimension
 *
 * @author jmburu
 */

/// Information required to build an irregular grid.
public class GridInfo {
    public BBox2 bbox;                     ///< Bounding box of the grid
    public int[]  dims = new int[2];       ///< Dimensions of the top level
    public int  max_snd_dim;               ///< Maximum second level density
    
    /// Returns the size of a top-level cell.
    public Float2 cell_size(){ return div(sub(bbox.max, bbox.min), new Float2(dims[0], dims[1])); }

    /// Returns the number of top-level cells.
    public int num_top_cells() { return dims[0] * dims[1]; }
    
    /// Maximum depth of grid
    public int max_depth(){ return max_snd_dim;}
    
    /// Maximum dims of all subcells in the main grid
    public int[] max_subcells_dims(){return new int[]{dims[0] << max_snd_dim, dims[1] << max_snd_dim};}
    
    /// Total subcell for each top cell
    public int total_subcells_top_cell(){return 1 << max_snd_dim;};
}
