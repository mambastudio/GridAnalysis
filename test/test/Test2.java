/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.utilities.list.IntegerList;



/**
 *
 * @author jmburu
 */
public class Test2 {
    public static void main(String... args)
    {
        IntegerList list = new IntegerList(new int[]{1, 1, 1, 1, 1});
        System.out.println(list);
        System.out.println(list.prefixSum());
        
    }
    
}
