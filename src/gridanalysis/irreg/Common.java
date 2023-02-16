/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 *
 * @author user
 */
public class Common {
    public final static float pi = 3.14159265359f;
    /// Number of bits to allocate to store the sub-level dimensions in the voxel map.
    public static int ENTRY_SHIFT = 4;
    
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
    
    public static<T> void clearFill(ArrayList list, int size, Supplier<T> supplier)
    {
        list.clear();
        for(int i = 0; i<size; i++)
            list.add(supplier.get());
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
