/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import java.util.ArrayList;

/**
 *
 * @author user
 */
public class Common {
    // A method to swap two ArrayList objects in Java
    public static<T> void swapArrayLists(ArrayList<T> list1, ArrayList<T> list2) {
        // Check if the lists are null or empty
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return;
        }
        // Create a temporary ArrayList to store the elements of list1
        ArrayList<T> temp = new ArrayList(list1);
        // Clear the elements of list1
        list1.clear();
        // Add the elements of list2 to list1
        list1.addAll(list2);
        // Clear the elements of list2
        list2.clear();
        // Add the elements of temp to list2
        list2.addAll(temp);
    }
    
    public static<T> void resize(ArrayList<T> list, int size)
    {
        
    }
    
    public static<T> void resize(ArrayList<T> list, int size, T value)
    {
        
    }
}
