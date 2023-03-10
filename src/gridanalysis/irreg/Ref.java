/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

/**
 *
 * @author jmburu
 */
/// Reference to a triangle with its associated cell index.
public class Ref {
    public int tri;        ///< Triangle index
    public int top_cell;   ///< Top-level cell index

    /// Position of the second-level cell encoded using a Morton scheme.
    /// This means 3 bits for each level of the octree, and each level stored consecutively.
    /// 64 bits allows to reach a depth of 21, which corresponds to a second level resolution of 2097152^3.
    long snd_cell;

    public Ref() {}
    public Ref(int tri, int top, long snd)        
    {this.tri = tri; this.top_cell = top; this.snd_cell = snd;}
    
    @Override
    public final String toString() {
        return String.format("(%1s, %1s)", tri, top_cell);
    }
}
