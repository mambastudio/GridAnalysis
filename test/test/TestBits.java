/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.utilities.BitUtility;

/**
 *
 * @author user
 */
public class TestBits {
    public static void main(String... args)
    {
        int i = BitUtility.get_bits_from(31, -1);
        //System.out.println(Integer.toBinaryString(i));
        //i = BitUtility.get_bits_from(0, i);
        System.out.println(Integer.toBinaryString(i));
    }
}
