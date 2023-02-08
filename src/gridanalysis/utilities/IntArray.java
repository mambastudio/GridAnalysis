/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 *
 * @author user
 */
public class IntArray {
    protected int[] array;
    protected final int offset;
    protected final int size;
            
    public IntArray(int[] array)
    {
        if(array == null)
            throw new NullPointerException("array is null");
        this.array = array;
        this.offset = 0;
        this.size = array.length;
    }
    
    private IntArray(IntArray intArray, int offset, int fromIndex, int toIndex)
    {
        rangeCheckBound(fromIndex, toIndex, intArray.size);
        this.array = intArray.array();
        this.offset = offset + fromIndex;
        this.size = toIndex - fromIndex;
    }
    
    public static int exclusiveScan(IntArray values, int n, IntArray result)
    {
        System.arraycopy(values.array, values.offset, result.array, result.offset, values.offset + n);
        Arrays.parallelPrefix(result.array, result.offset, result.offset + n, (a, b)-> a+b);
        System.arraycopy(result.array, result.offset, result.array, result.offset + 1, n - 1);
        result.set(0, 0);
        return result.get(n - 1);
    }
    
    public static void sort_pairs(
            IntArray key_in, IntArray value_in,
            IntArray key_out, IntArray value_out)
    {
        if(!(key_in.size == value_in.size && 
             value_in.size == key_out.size &&
             key_out.size == value_out.size))
            throw new UnsupportedOperationException("Mismatch of IntArrays");
        
        int[] valueOutput =
            // original indices: ascending numbers from 0 to array length
            IntStream.range(key_in.offset, key_in.offset + key_in.size)
            // sort using the values of the first array
           .boxed().sorted(Comparator.comparingInt(ix->key_in.array()[ix]))
            // apply to the values of the second array
           .mapToInt(ix->value_in.array()[ix])
            // create a result array
           .toArray();
        
        int[] keyOutput = key_in.getCopyRangeArray();
        Arrays.sort(keyOutput);
        
        System.arraycopy(keyOutput, 0, key_out.array, key_out.offset, keyOutput.length);
        System.arraycopy(valueOutput, 0, value_out.array, value_out.offset, valueOutput.length);
    }
    
    public static int partition(IntArray input, IntArray output, int n, IntArray flags)
    {
        int selected_index = 0;
        int remaining_index = n - 1;
        for (int i = 0; i < n; i++) {
            if (flags.get(i) == 1) {
                output.set(selected_index++, input.get(i));
            } else {
                output.set(remaining_index--, input.get(i));
            }
        }

        return selected_index;
    }   
    
    public static IntArray createFromArray(int... array)
    {
        return new IntArray(array);
    }
    
    public static IntArray createFromSize(int size)
    {
        return new IntArray(new int[size]);
    }
    
    public IntArray getSubArray(int start, int end)
    {
        return new IntArray(this, offset, start, end);
    }
    
    public IntArray splitSubArrayFrom(int start)
    {
        return getSubArray(start, this.size());
    }
    
    public void set(int index, int value)
    {
        rangeCheck(index);
        this.array[offset + index] = value;
    }
    
    public int get(int index)
    {
        rangeCheck(index);
        return this.array[offset + index];
    }
    
    public int[] getCopyRangeArray()
    {
        return Arrays.copyOfRange(array, offset, offset + size);
    }
    
    public int[] array()
    {
        return array;
    }
    
    public void copyTo(IntArray destinationArray)
    {
        if(size() != destinationArray.size)
            throw new UnsupportedOperationException("mismatch of array size");
        System.arraycopy(array(), offset, destinationArray.array(), destinationArray.offset, size());        
    }
    
    public void fill(int value, int n)
    {
        if(n > size())
            throw new UnsupportedOperationException("n is greater than int array size");
        Arrays.fill(array, offset, offset + n, value);
    }
    
    public void fillOne(int n)
    {
        fill(1, n);
    }
    
    public void setArray(int[] array)
    {
        if(this.array.length != array.length)
            throw new UnsupportedOperationException("no swap since the two arrays are not equal");
        this.array = array;
    }
    
    public int size()
    {
        return size;
    }
    
    private void rangeCheck(int index) {
        if (index < 0 || index >= this.size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+this.size;
    }
    
    
    private void rangeCheckBound(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }
    
    public void swap(IntArray array)
    {
        if(this.array.length != array.array().length)
           throw new UnsupportedOperationException("no swap since the two arrays are not equal");
        
        int[] temp = this.array;
        this.array = array.array();
        array.setArray(temp);
    }
    
    public static IntArray getArrayWithIndices(int size)
    {
        IntArray array = new IntArray(new int[size]);
        for(int i = 0; i<array.size(); i++)
            array.set(i, i);
        return array;
    }
    
    
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        int[] arr = getCopyRangeArray();
        for(int i : arr)
            builder.append(String.format("%3s",i));
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof IntArray)
        {
            IntArray intArr = (IntArray)obj;
            return array == intArr.array();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Arrays.hashCode(this.array);
        return hash;
    }
}
