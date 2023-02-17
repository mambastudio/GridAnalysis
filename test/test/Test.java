/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.utilities.Utility;

/**
 *
 * @author jmburu
 */
public class Test {
    public static void main(String... args)
    {
        int pop_count[] = { 0, 1, 1, 2,
                            1, 2, 2, 3,
                            1, 2, 2, 3,
                            2, 3, 3, 4};
        
        int flag = Integer.parseInt("01111", 2); 
        
        System.out.println(pop_count[flag]);
       
        
    }
}
