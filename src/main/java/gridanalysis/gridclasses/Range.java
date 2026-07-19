/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

/**
 *
 * @author user
 */
public class Range {
    public int lx, ly;
    public int hx, hy;
    public Range() {}
    public Range(int lx, int ly,
                 int hx, int hy)      
    {
        this.lx = lx;
        this.ly = ly;       
        this.hx = hx;
        this.hy = hy;        
    }
    public int size() { return (hx - lx + 1) * (hy - ly + 1); }
}
