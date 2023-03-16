/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities.list;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.function.BiPredicate;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;

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
    public void set(I list){set(0, list);}
    public void set(int index, I list){set(index, list.trimCopy());}
    public abstract void set(int index, int[] value);        
    public abstract void increment(int index);
    public abstract void decrement(int index);
    public abstract I getSubList(int fromIndex, int toIndex);
    public I getSubListFrom(int fromIndex){return getSubList(fromIndex, size());}
    public abstract void remove(int index);     
    public abstract void remove(int fromIndex, int toIndex);
    public abstract int size();    
    public abstract int end();  
    public abstract int back();      
    public int[] trimCopy(){return trimCopy(0, size());}
    protected abstract int[] trimCopy(int fromIndex, int toIndex);
    public abstract int[] trim();   
    public abstract void clear();
    public abstract void resize(int size);
    public abstract void resize(int size, int value);
    
    public int max(){return reduce(Integer.MIN_VALUE, (a, b) -> Math.max(a, b));}
      
    public  int prefixSum() {return prefixSum(0, size());}
    public abstract int prefixSum(int fromIndex, int toIndex);
    
    public  int reduce() {return reduce(0, size());}
    public abstract int reduce(int fromIndex, int toIndex);
    
    public  int reduce(int identity, IntBinaryOperator op) {return reduce(0, size(), identity, op);}
    public abstract int reduce(int fromIndex, int toIndex, int identity, IntBinaryOperator op);
    
    public void sort(){sort(0, size(), (a, b) -> a > b);}
    public void sort(BiPredicate<Integer, Integer> op){sort(0, size(), op);}
    public void sort(int fromIndex, int toIndex, BiPredicate<Integer, Integer> op){sort_pairs(fromIndex, toIndex, null, op);}
    
    public void sort_pairs(I values){sort_pairs(0, size(), values, (a, b) -> a > b);}
    public abstract void sort_pairs(int fromIndex, int toIndex, I values, BiPredicate<Integer, Integer> op);
    
    public int partition_stable(I output, I flags){return partition_stable(0, size(), output, flags);}    
    public int partition_stable(int n, I output, I flags){return partition_stable(0, n, output, flags);}    
    public abstract int partition_stable(int fromIndex, int toIndex, I output, I flags); //output, flags sizes should be equal or larger than (toIndex - fromIndex)
        
    public I transform(IntFunction<Integer> function){return transform(0, size(), function);}
    public abstract I transform(int fromIndex, int toIndex, IntFunction<Integer> function);
    
    public void transform(I output, IntFunction<Integer> function){transform(0, size(), output, function);}
    public abstract void transform(int fromIndex, int toIndex, I output, IntFunction<Integer> function);
        
    public int find(int value){return find(0, size(), value);};
    public abstract int find(int first, int end, int value);    
    
    public void fill(int value){fill(0, size(), value);}
    public abstract void fill(int fromIndex, int toIndex, int value);    
    
    public void shiftRight(int steps){shiftRight(0, size(), steps);}
    protected abstract void shiftRight(int fromIndex, int toIndex, int steps);
            
    public abstract void swapElement(int index1, int index2);
    public abstract void swap(I list);
     
    
    
    @Override
    public abstract String toString();
    
    protected void sizeRangeCheck(int fromIndex, int toIndex, int size)
    {
        if((toIndex - fromIndex) > size)
            throw new IndexOutOfBoundsException("size range out of bound: index size range - " 
                    +(toIndex - fromIndex)+ " target size bound - " +size);
    }
    
    protected boolean isInRange(float index, int fromIndex, int toIndex)
    {
        if(index < fromIndex)
            return false;
        else return index < toIndex;
    }
    
    protected void rangeAboveZero(int index)
    {
        if (index < 1)
            throw new IndexOutOfBoundsException("index out of bound " +index);
    }
        
    protected void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("index out of bound at index " +index+ " whereby available size is " +size);
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
        final int expectedModCount = modCount;
        if(minCapacity - array.length > 0)
            grow(minCapacity);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;    
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
    
    //log2
    protected int log2nlz(int bits )
    {
        if( bits == 0 )
            throw new UnsupportedOperationException("value should be zero");
        return 31 - Integer.numberOfLeadingZeros( bits );
    }
    
    protected void compatibleCheck(I list)
    {
        if(list.size() != size())
            throw new UnsupportedOperationException("list not compatible");
    }
    
    protected void compatibleCheck(int fromIndex, int toIndex, I list)
    {
        if(list.size() != toIndex - fromIndex)
            throw new UnsupportedOperationException("list not compatible");
    }
}
