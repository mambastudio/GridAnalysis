/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import static gridanalysis.irreg.Float2.add;
import static gridanalysis.irreg.Float2.sub;

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
    
    public Tri2(Float2 v0, Float2 v1, Float2 v2) {
        init(v0, v1, v2);
    }
        
    private void init(Float2 v0, Float2 v1, Float2 v2)
    {
        this.v0 = v0;
        this.e1 = sub(v0, v1);
        this.e2 = sub(v2, v0);
    }
    
    /// Packs the normal components into a float2 structure.
    public Float2 normal() { return new Float2(nx, ny); }
    
    public BBox2 bbox(){
        BBox2 box = new BBox2();
        box.extend(v0);
        box.extend(sub(v0, e1));
        box.extend(add(v0, e2));
        return box;
    }
}
