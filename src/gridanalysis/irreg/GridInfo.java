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
}
