/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.irreg.IntVector;
import gridanalysis.irreg.MergePair;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author jmburu
 */
public class Test2 {
    public static void main(String... args)
    {
        // Create an IntVector object with an int array of size 5
        IntVector cache = new IntVector(5);
        // Set the elements of the int array to 1, 2, 3, 4, 5
        for (int i = 0; i < cache.length(); i++) {
          cache.set(i, i + 1);
        }
        
        System.out.println(cache);
        
        // Create a sub IntVector object with the range [1, 3)
        IntVector sub = cache.subCache(1, 3);
        // Print the sub IntVector object
        System.out.println(sub); // [2, 3]
        // Add an element 6 to the end of the original IntVector object
        cache.add(6);
        // Print the original IntVector object
        System.out.println(cache); // [1, 2, 3, 4, 5, 6]
        // Print the sub IntVector object
        System.out.println(sub); // [2, 3]
        // Remove the element at index 2 of the original IntVector object
        cache.remove(2);
        // Print the original IntVector object
        System.out.println(cache); // [1, 2, 4, 5, 6]
        // Print the sub IntVector object
        System.out.println(sub); // [2, 4]
    }
    
}
