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
 * @param <BaseIntList>
 */
public abstract class IntListAbstract<BaseIntList extends BaseIntegerList> implements BaseIntegerList<BaseIntList>  {
    
    protected static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    protected int[] array;
    protected int size;  
    protected int modCount = 0;
        
    @Override
    public final void set(BaseIntList list){set(0, list);}        
    public final int end(){return size();}  
    public final int back(){return get(size()-1);}     
    public final void clear(){remove(0, size());}
        
    public int max(){return reduce(Integer.MIN_VALUE, (a, b) -> Math.max(a, b));}
      
    public  int prefixSum() {return prefixSum(0, size());}
    public abstract int prefixSum(int fromIndex, int toIndex);
    
    public  int reduce() {return reduce(0, size());}
    public abstract int reduce(int fromIndex, int toIndex);
    
    public  int reduce(int identity, IntBinaryOperator op) {return reduce(0, size(), identity, op);}
    public abstract int reduce(int fromIndex, int toIndex, int identity, IntBinaryOperator op);
    
    public void sort(){sort(0, size(), (a, b) -> a > b);}
    public void sort(BiPredicate<Integer, Integer> op){sort(0, size(), op);}
    protected void sort(int fromIndex, int toIndex, BiPredicate<Integer, Integer> op){sort_pairs(fromIndex, toIndex, null, op);}
    
    public void sort_pairs(BaseIntList values){sort_pairs(0, size(), values, (a, b) -> a > b);}
    protected abstract void sort_pairs(int fromIndex, int toIndex, BaseIntList values, BiPredicate<Integer, Integer> op);
    
    public int partition_stable(BaseIntList output, BaseIntList flags){return partition_stable(0, size(), output, flags);}    
    public int partition_stable(int n, BaseIntList output, BaseIntList flags){return partition_stable(0, n, output, flags);}    
    protected abstract int partition_stable(int fromIndex, int toIndex, BaseIntList output, BaseIntList flags); //output, flags sizes should be equal or larger than (toIndex - fromIndex)
        
    public BaseIntList transform(IntFunction<Integer> function){return transform(0, size(), function);}
    public abstract BaseIntList transform(int fromIndex, int toIndex, IntFunction<Integer> function);
    
    public void transform(BaseIntList output, IntFunction<Integer> function){transform(0, size(), output, function);}
    public abstract void transform(int fromIndex, int toIndex, BaseIntList output, IntFunction<Integer> function);
        
    public int find(int value){return find(0, size(), value);}; //O(n) to be removed?
    public abstract int find(int first, int end, int value);    
    
    public void fill(int value){fill(0, size(), value);}
    public abstract void fill(int fromIndex, int toIndex, int value);    
    
    public void shiftRight(int steps){shiftRight(0, size(), steps);}
    protected abstract void shiftRight(int fromIndex, int toIndex, int steps);
                
    @Override
    public abstract String toString();
    
    //CHECK IF TO BE DELETED. This is somehow similar but not exact to rangeCheckBound(int fromIndex, int toIndex, int size)
    protected final void sizeRangeCheck(int fromIndex, int toIndex, int size)
    {
        if((toIndex - fromIndex) > size)
            throw new IndexOutOfBoundsException("size range out of bound: index size range - " 
                    +(toIndex - fromIndex)+ " target size bound - " +size);
    }
    
    //toIndex is exclusive
    protected final boolean isInRange(float index, int fromIndex, int toIndex)
    {
        if(index < fromIndex)
            return false;
        else return index < toIndex;
    }
    
    protected final void rangeAboveZero(int index)
    {
        if (index < 1)
            throw new IndexOutOfBoundsException("index out of bound " +index);
    }
        
    protected final void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("index out of bound at index " +index+ " whereby available size is " +size);
    }
    
    //Size should encompass the fromIndex and toIndex 
    protected final void rangeCheckBound(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex is less than zero " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex is greater than size, toIndex = " + toIndex + " size = " +size);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }
    
    protected final void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("index out of bound " +index);
    }
        
    
    //monitor changes when it grows, hence when used in a method, no monitoring changes required in the method
    protected final void ensureCapacity(int minCapacity)
    {
        final int expectedModCount = modCount;
        if(minCapacity - array.length > 0)
            grow(minCapacity);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;    
    }
    
    protected final void grow(int minCapacity)
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
    
    protected final int hugeCapacity(int minCapacity) 
    {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
    
    //log2
    protected final int log2nlz(int bits )
    {
        if( bits == 0 )
            throw new UnsupportedOperationException("value should be zero");
        return 31 - Integer.numberOfLeadingZeros( bits );
    }
    
    
    protected final void compatibleCheck(BaseIntList list)
    {
        if(list.size() != size())
            throw new UnsupportedOperationException("list not compatible");
    }
    
    protected final void compatibleCheck(int fromIndex, int toIndex)
    {
        rangeAboveZero(toIndex);
        if(size() != toIndex - fromIndex)
            throw new UnsupportedOperationException("list not compatible");
    }
}
