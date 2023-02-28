/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities.list;

import java.util.Arrays;

/**
 *
 * @author user
 * @param <I>
 */
public abstract class IntListAbstract<I extends IntListAbstract>  {
    
    protected static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    protected int[] array;
    protected int size;  
    protected int modCount = 0;
        
    public abstract void add(int value);   
    public abstract void add(int index, int value);
    public abstract void add(int index, int[] value);
    public abstract int get(int index);
    public abstract void set(int index, int value);
    public abstract void set(int index, int[] value);
    public abstract int[] trim();
    public abstract void increment(int index);
    public abstract void decrement(int index);
    public abstract I getSubList(int fromIndex, int toIndex);
    public I getSublistFrom(int fromIndex){return getSubList(fromIndex, size());}
    public abstract void remove(int index);     
    public abstract void remove(int fromIndex, int toIndex);
    public abstract int size();    
    public abstract int end();  
    public abstract int back();      
    public abstract int[] toArray();
    public abstract void clear();
    public abstract void resize(int size);
    public abstract void resize(int size, int value);
    
    public abstract IntegerList prefixSum();
    public abstract void swap(IntegerList list);
    public abstract int find(int first, int end, int value);        
    
    @Override
    public abstract String toString();
    
    protected void rangeAboveZero(int index)
    {
        if (index < 1)
            throw new IndexOutOfBoundsException("index out of bound " +index);
    }
        
    protected void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("index out of bound " +index);
    }
    
    protected void rangeCheckBound(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }
    
    protected void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("index out of bound " +index);
    }
        
    protected void ensureCapacity(int minCapacity)
    {
        modCount++;
        if(minCapacity - array.length > 0)
            grow(minCapacity);
    }
    
    protected void grow(int minCapacity)
    {
        int oldCapacity = array.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        array = Arrays.copyOf(array, newCapacity);
    }
    
    protected int hugeCapacity(int minCapacity) 
    {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
}
