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
public class Range2 {
    int lx; int hx;
    int ly; int hy;

    public Range2() {}
    public Range2(int lx, int hx,
                  int ly, int hy)
    {
        this.lx = lx; this.hx = hx;
        this.ly = ly; this.hy = hy;
    }

    int size() { return (hx - lx + 1) * (hy - ly + 1); }

    
    public void iterate(ConsumerInt2 f){        
        for (int y = ly; y <= hy; y++) {
            for (int x = lx; x <= hx; x++) {
                f.function(x, y);
            }           
        }
    }
}
