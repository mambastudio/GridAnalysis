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
/// Triangle represented with one vertex and two edges (v0, v0 - v1, v2 - v0)
/// to facilitate intersection. Also contains the normal in a packed form.
public class Tri2 {
    public Float2 v0;
    public float nx;
    public Float2 e1;
    public float ny;
    public Float2 e2;    
    
    /// Packs the normal components into a float2 structure.
    public Float2 normal() { return new Float2(nx, ny); }
    
    public BBox2 bbox(){
        return null;
    }
}
