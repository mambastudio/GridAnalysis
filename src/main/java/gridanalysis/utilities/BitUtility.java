/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities;

/**
 *
 * @author user
 * 
 * TODO: Comment in detail, might become a future standard in my code
 * 
 */
public class BitUtility {
    
    public static int int_mask()
    {
        //bit count is always 32
        return -1;
    }
    public static int int_mask(int size)
    {
        checkSizeInRange(size);
        return (size == 32) ? -1 : (1 << size) - 1;
    }
    
    public static int int_mask_until(int index)
    {
        checkIndexInRange(index);
        return (index == 31) ? -1 : (1 << (index + 1)) - 1;
    }
    
    //how rgb color works
    public static int apply_bits_at(int index, int value, int output)
    {
        checkIndexInRange(index);
        return output | value << index;        
    }
    
    //how rgb color works
    public static int get_bits_at(int index, int value, int size)
    {
        checkIndexInRange(index);
        return (value >> index) & int_mask(size);
    }
    
    public static int get_bits_from(int index, int value)
    {
        checkIndexInRange(index);
        return get_bits_at(index, value, 32 - index);
    }
    
    private static void checkIndexInRange(int index)
    {       
        if(index < 0 || index > 31)
            throw new UnsupportedOperationException("index is out of range");
    }
    
    private static void checkSizeInRange(int size)
    {
        if(size < 1 || size > 32)
            throw new UnsupportedOperationException("index is out of range");
    }
}
