/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Arrays;

/**
 *
 * @author jmburu
 */
public class Test4 {
    public static void main(String... args)
    {
        int[] input = new int[1000];
        Arrays.fill(input, 1);
        
        System.out.println(Arrays.toString(input));
        
        // Step 1: Adjust input array for exclusive prefix sum
        Arrays.parallelSetAll(input, i -> i > 0 ? input[i - 1]: input[i]);
        input[0] = 0;

        // Step 2: Compute inclusive prefix sum
        Arrays.parallelPrefix(input, Integer::sum);

        // Print the exclusive prefix sum
        System.out.println(Arrays.toString(input));
    }
}
