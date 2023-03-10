/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import gridanalysis.utilities.list.IntegerList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static java.util.stream.Collectors.partitioningBy;

/**
 *
 * @author user
 */
public class Common {
    public final static float pi = 3.14159265359f;
    
    
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
    
    public static<T> void resize(ArrayList<T> list, int size, Supplier<T> supply)
    {
        if (size < 1)
            throw new IndexOutOfBoundsException("index out of bound " +size);
        ArrayList<T> arrayListNew = new ArrayList(size);
        for(int i = 0; i<size; i++)
            arrayListNew.add(supply.get()); 
        
        if(size < list.size())
            Collections.copy(arrayListNew, list.subList(0, size));
        else         
            Collections.copy(arrayListNew, list);
        
        list.clear();
        list.addAll(arrayListNew);
    }
    
    public static<T> int partition(List<T> list, Predicate<T> predicate)
    {
        Map<Boolean, List<T>> result = list.stream().collect(partitioningBy(predicate));
        List<T> trueList = result.get(true);
        List<T> falseList = result.get(false);
        int index = trueList.size();
        
        list.clear();
        list.addAll(trueList);
        list.addAll(falseList);
        
        return index;
    }
    
    public static<T> void clearFill(ArrayList list, int size, Supplier<T> supplier)
    {
        list.clear();
        for(int i = 0; i<size; i++)
            list.add(supplier.get());
    }
    
    /// Determines if the given range of references is a subset of the other
    public static boolean is_subset(IntegerList p0, int c0, IntegerList p1, int c1) {
        if (c1 > c0) return false;
        if (c1 == 0) return true;

        int i = 0, j = 0;

        do {
            int a = p0.get(i);
            int b = p1.get(j);
            if (b < a) return false;
            j += ((a == b) ? 1 : 0);
            i++;
        } while (i < c0 & j < c1);

        return j == c1;
    }

    public static float radians(float x) {
        return x * pi / 180.0f;
    }

    public static float degrees(float x) {
        return x * 180.0f / pi;
    }

    
    public static float clamp(float a, float b, float c) {
        return (a < b) ? b : ((a > c) ? c : a);
    }

    public static int float_as_int(float f) {
        return Float.floatToIntBits(f);
    }

    public static float int_as_float(int i) {
        return Float.intBitsToFloat(i);
    }

    
    float lerp(float a, float b, float u) {
        return a * (1 - u) + b * u;
    }

    
    float lerp(float a, float b, float c, float u, float v) {
        return a * (1 - u - v) + b * u + c * v;
    }

    public static int closest_log2(int k) {
        // One could use a CLZ instruction if the hardware supports it
        int i = 0;
        while ((1 << i) < k) i++;
        return i;
    }
}
