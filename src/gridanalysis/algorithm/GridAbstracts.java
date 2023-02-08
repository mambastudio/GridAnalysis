/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.algorithm;

/**
 *
 * @author user
 */
public abstract class GridAbstracts {
    public int __ffs(int value)
    {
        int pos = 1;
        while ((value & 1) == 0 && value != 0) {
            value >>= 1;
            pos++;
        }
        return (value == 0) ? 0 : pos;
    }
    
    public int __popc(int mask) {
        return Integer.bitCount(mask);
    }
}
