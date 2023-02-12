/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities;

import java.util.Arrays;

/**
 *
 * @author user
 * @param <T>
 */
public class ObjArray<T> {
    protected T[] array;
    protected final int offset;
    protected final int size;
            
    public ObjArray(T[] array)
    {
        if(array == null)
            throw new NullPointerException("array is null");
        this.array = array;
        this.offset = 0;
        this.size = array.length;
    }
    
    private ObjArray(ObjArray<T> objArray, int offset, int fromIndex, int toIndex)
    {
        rangeCheckBound(fromIndex, toIndex, objArray.size);
        this.array = objArray.array();
        this.offset = offset + fromIndex;
        this.size = toIndex - fromIndex;
    }
        
    public ObjArray getSubArray(int start, int end)
    {
        return new ObjArray(this, offset, start, end);
    }
    
    public ObjArray splitSubArrayFrom(int start)
    {
        return getSubArray(start, this.size());
    }
    
    public void set(int index, T value)
    {
        rangeCheck(index);
        this.array[offset + index] = value;
    }
    
    public T get(int index)
    {
        rangeCheck(index);
        return this.array[offset + index];
    }
    
    public T[] getCopyRangeArray()
    {
        return Arrays.copyOfRange(array, offset, offset + size);
    }
    
    public T[] array()
    {
        return array;
    }
    
    public void copyTo(ObjArray destinationArray)
    {
        if(size() != destinationArray.size)
            throw new UnsupportedOperationException("mismatch of array size");
        System.arraycopy(array(), offset, destinationArray.array(), destinationArray.offset, size());        
    }
        
    public void setArray(T[] array)
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
    
    public void swap(ObjArray<T> array)
    {
        if(this.array.length != array.array().length)
           throw new UnsupportedOperationException("no swap since the two arrays are not equal");
        
        T[] temp = this.array;
        this.array = array.array();
        array.setArray(temp);
    }
       
    @Override
    public String toString()
    {
        if (array == null)
            return "null";
        int iMax = size() - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = offset; i<(offset + size()); i++) {
            b.append(array[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
        throw new UnsupportedOperationException("ObjArray has an issue");
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof ObjArray)
        {
            ObjArray intArr = (ObjArray)obj;
            return array == intArr.array();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);        
    }
}
