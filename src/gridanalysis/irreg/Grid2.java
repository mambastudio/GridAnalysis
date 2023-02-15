/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import gridanalysis.utilities.IntArray;

/**
 *
 * @author user
 */
public class Grid2 {
    /// Counts the number of elements in the union of two sorted arrays.
    public static int count_union(IntArray p0, int c0, IntArray p1, int c1) {
        int i = 0;
        int j = 0;

        int count = 0;
        while (i < c0 & j < c1) {
            int k0 = p0.get(i) <= p1.get(j) ? 1 : 0;
            int k1 = p0.get(i) >= p1.get(j) ? 1 : 0;
            i += k0;
            j += k1;
            count++;
        }

        return count + (c1 - j) + (c0 - i);
    }
}
