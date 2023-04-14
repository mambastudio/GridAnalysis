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
import java.util.stream.IntStream;

/**
 *
 * @author user
 * 
 * Simple List that behaves like an ArrayList but for integer primitive
 * Inspired by jdk 1.8 ArrayList and CUDA Cub
 * 
 * 
 * TODO:
 *  - Primitive iterator - https://javadevcentral.com/primitiveiterator-in-java
 * 
 */
public class IntegerList extends IntListAbstract<IntegerList> {
    
    public IntegerList()
    {
        this.array = new int[10];       
        this.size = 0;
    }
    
    public IntegerList(int[] array)
    {
        if(array == null)
            throw new NullPointerException("array is null");
        this.array = array;        
        this.size = array.length;
    }
    
    public IntegerList(int size, int value)
    {
        if(size < 1)
            throw new IndexOutOfBoundsException("size is less than 1");
        this.array = new int[size];    
        fill(0, array.length, value);
        this.size = array.length;
    }

    @Override
    public void add(int value) {
        //add and remove have to modify the modcount
        ensureCapacity(size + 1); //increments modCount
        array[size] = value;    
        size++;
    }

    @Override
    public int get(int index) {
        rangeCheck(index);
        return this.array[index];
    }

    @Override
    public void set(int index, int value) {
        rangeCheck(index);               
        this.array[index] = value;       
    }
    
    @Override
    public void set(int index, int[] value) {       
        rangeCheckBound(index, index + value.length, size()); 
        System.arraycopy(value, 0, array, index, value.length);              
    }
    
    @Override
    public void set(int index, IntegerList list) {
        rangeCheck(index);        
        rangeCheckBound(index, index + list.size(), size());        
        System.arraycopy(list.trim(), list.rootOffset(), array, index, list.size());
    }


    @Override
    public void increment(int index) {
        rangeCheck(index);
        final int expectedModCount = modCount;
        array[index]++;
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;   
    }

    @Override
    public void decrement(int index) {
        rangeCheck(index);
        final int expectedModCount = modCount;
        array[index]--;
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    public IntegerList getSubList(int fromIndex, int toIndex) {
        rangeCheckBound(fromIndex, toIndex, size);
        return new IntegerSubList(this, fromIndex, toIndex);
    }
        
    @Override
    public IntegerList getSubListFrom(int fromIndex) {
        rangeCheckBound(fromIndex, fromIndex, size());
        return new IntegerSubList(this, fromIndex, size());
    }
    
    @Override
    public void remove(int index) {
        rangeCheck(index);
        
        final int expectedModCount = modCount;
        int numMoved = size - (index) - 1;
        if (numMoved > 0)
            System.arraycopy(array, index + 1, array, index,
                             numMoved);
        size--;
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
   
    @Override
    public void remove(int fromIndex, int toIndex) {
        rangeCheckBound(fromIndex, toIndex, size);
        
        final int expectedModCount = modCount;
        int numMoved = size - toIndex;
        System.arraycopy(array, toIndex, array, fromIndex,
                         numMoved);
        int newSize = size - (toIndex-fromIndex);
        size = newSize;
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void add(int index, int value) {
        rangeCheckForAdd(index);
        ensureCapacity(size + 1);  //add and remove have to modify the modcount
        System.arraycopy(array, index, array, index + 1,
                         size - index);
        array[index] = value;
        size++;
    }
 
    @Override
    public void add(int index, int[] arr) {
        rangeCheckForAdd(index);
        int numNew = arr.length;
        ensureCapacity(size + numNew);  // Increments modCount
        
        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(array, index, array, index + numNew,
                             numMoved);
        
        System.arraycopy(arr, 0, array, index, numNew);
        size += numNew;
    }
    
    @Override
    public void fill(int fromIndex, int toIndex, int value)
    {
        rangeCheckBound(fromIndex, fromIndex, size);
        final int expectedModCount = modCount;
        for(int i = fromIndex; i<toIndex; i++)
            array[i] = value;        
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    public int[] trimCopy() {    
        return Arrays.copyOfRange(trim(), 0, size);   
    }
    
    @Override
    public int[] trimCopy(int fromIndex, int toIndex)
    {
        rangeCheckBound(fromIndex, fromIndex, size);
        return Arrays.copyOfRange(trim(), fromIndex, toIndex);
    }
    
    @Override
    public int[] trim() {       
        if(size < array.length)        
            array = Arrays.copyOfRange(array, 0, size);        
        return array;
    }
    
    @Override
    public String toString() {
        return Arrays.toString(trimCopy());
    }

    @Override
    public void resize(int size) {
        rangeAboveZero(size);               
        if(size < size())        
            remove(size, size());        
        else
        {
            int[] arrayNew = new int[size - size()];
            add(size(), arrayNew);
        }
    }

    @Override
    public void resize(int size, int value) {
        rangeAboveZero(size);               
        if(size < size())        
            remove(size, size());        
        else
        {
            int[] arrayNew = new int[size - size()];
            Arrays.fill(arrayNew, 0, arrayNew.length, value);
            add(size(), arrayNew);
        }
    }
    
    @Override
    public int prefixSum(int fromIndex, int toIndex)
    {
        rangeCheckBound(fromIndex, fromIndex, size);
        final int expectedModCount = modCount;
        Arrays.parallelPrefix(array, fromIndex, toIndex, (x, y) -> x + y);
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
        return get(toIndex-1);
    }
    
    @Override
    public int reduce(int fromIndex, int toIndex)
    {
        rangeCheckBound(fromIndex, toIndex, size);
        final int expectedModCount = modCount;
        int count = Arrays.stream(array, fromIndex, toIndex)
                .parallel()
                .sum();
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
        return count;        
    }
    
    @Override
    public int reduce(int fromIndex, int toIndex, int identity, IntBinaryOperator op)
    {
        rangeCheckBound(fromIndex, toIndex, size);
        final int expectedModCount = modCount;
        int count = Arrays.stream(array, fromIndex, toIndex)
                .parallel()
                .reduce(identity, op);
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
        return count;        
    }
        
    //don't override in sublist
    @Override
    public void swap(IntegerList list)
    {
        if(!this.isSubList() && !list.isSubList()) //both are root list
        {            
            if(this.size() == list.size()) //equal size
            {
                int[] temp = array;
                this.array = list.array;
                list.array = temp;
            }
            else //not equal in size, notify children it has been modified
            {
                final int expectedModCount = modCount;

                int[] temp = array;
                this.array = list.array;
                list.array = temp;
               
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                modCount++;
            }                
        }
        else //both or one is sublist (that's why we don't override in the sublist)
        {            
            //if both are empty, no point to swap
            if(list.size() == 0 && this.size() == 0)
                return;
            
            compatibleCheck(list);
            int[] temp = trimCopy();
            set(0, list);
            list.set(0, temp);        
        }
    }
    
    @Override
    public void swapElement(int index1, int index2)
    {
        rangeCheck(index1);
        rangeCheck(index2);       
        int temp = get(index1);
        array[index1] = get(index2);
        array[index2] = temp;        
    }
            
    //Parallel Butterfly Sorting Algorithm on GPU by Bilal et al    
    @Override
    protected void sort_pairs(int fromIndex, int toIndex, IntegerList values, BiPredicate<Integer, Integer> op)
    {
        values.compatibleCheck(fromIndex, toIndex);
        final int expectedModCount = modCount;
        int inner_expectedModCount = values.modCount;
        
        int radix  = 2;
        int until = until(toIndex - fromIndex);
        int sizeList = toIndex - fromIndex;
        int T = (int) (Math.pow(radix, until)/radix);//data.length/radix if n is power of 2;

        for(int xout = 1; xout<=until; xout++)
        {            
            double[] PowerX = new double[]{Math.pow(radix, xout)};
            IntStream.range(0, T)
                    .parallel()
                    .forEach(t->{                        
                        if(t >= sizeList)
                            return;
                        
                        int yIndex      = (int) (t/(PowerX[0]/radix));  
                        int kIndex      = (int) (t%(PowerX[0]/radix));
                        int PosStart    = (int) (kIndex + yIndex * PowerX[0]);
                        int PosEnd      = (int) (PowerX[0] - kIndex - 1 + yIndex * PowerX[0]);
                        
                        if(!isInRange(PosStart + fromIndex, fromIndex, toIndex)) 
                            return;
                        if(!isInRange(PosEnd + fromIndex, fromIndex, toIndex)) 
                            return;
                        
                        if(op.test(get(PosStart + fromIndex), get(PosEnd + fromIndex)))
                        {
                            swapElement(PosStart + fromIndex, PosEnd + fromIndex);
                            values.swapElement(PosStart, PosEnd);
                        }
                    });
            if(xout > 1)
            {                
                for(int xin = xout; xin > 0; xin--)
                {
                    PowerX[0] = (Math.pow(radix, xin));
                    IntStream.range(0, T)
                        .parallel()
                        .forEach(t->{      
                            if(t >= sizeList)
                                return;
                            
                            int yIndex      = (int) (t/(PowerX[0]/radix));  
                            int kIndex      = (int) (t%(PowerX[0]/radix));
                            int PosStart    = (int) (kIndex + yIndex * PowerX[0]);
                            int PosEnd      = (int) (kIndex + yIndex * PowerX[0] + PowerX[0]/radix);
                            
                            if(!isInRange(PosStart + fromIndex, fromIndex, toIndex)) 
                                return;
                            if(!isInRange(PosEnd + fromIndex, fromIndex, toIndex)) 
                                return;

                            if(op.test(get(PosStart + fromIndex), get(PosEnd + fromIndex)))
                            {
                                swapElement(PosStart + fromIndex, PosEnd + fromIndex);
                                values.swapElement(PosStart, PosEnd);
                            }
                        });
                }
            }                    
        }
        
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
                
        if (values.modCount != inner_expectedModCount) {
            throw new ConcurrentModificationException();
        }
        values.modCount++;        
    }
    
    public static void sort_pairs(IntegerList keys_in, IntegerList values_in, IntegerList keys_out, IntegerList values_out) 
    {
        keys_in.compatibleCheck(values_in);
        values_in.compatibleCheck(keys_out);
        keys_out.compatibleCheck(values_out);

        keys_out.set(keys_in);
        values_out.set(values_in);
        
        keys_out.sort_pairs(values_out);
    }
            
    
    private  int until(int size)
    {
        int log2 = log2nlz(size);
        int difference = (int)(Math.pow(2, log2)) - size;

        if(difference == 0) return log2;
        else                return log2+1;
    }

    @Override
    public IntegerList transform(int fromIndex, int toIndex, IntFunction<Integer> function) {
        rangeCheckBound(fromIndex, toIndex, size);       
        final int expectedModCount = modCount;
        IntegerList list = new IntegerList(new int[toIndex - fromIndex]);
        IntStream.range(fromIndex, toIndex)
                .parallel()
                .forEach(i->{
                    list.array[i - fromIndex] = function.apply(get(i));                    
                });
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
        return list;
    }

    @Override
    public void transform(int fromIndex, int toIndex, IntegerList output, IntFunction<Integer> function) {
        rangeCheckBound(fromIndex, toIndex, size);    //check if fromIndex and toIndex are in range of this list 
        sizeRangeCheck(fromIndex, toIndex, output.size()); //check if output size is larger than range fromIndex to toIndex
        final int expectedModCount = modCount;
        
        IntStream.range(fromIndex, toIndex)
                .parallel()
                .forEach(i->{                    
                    output.set(i - fromIndex, function.apply(get(i))); //output usually starts at zero, hence subtract by fromIndex
                });
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
    }

    //partition while maintaining order
    @Override
    public int partition_stable(int fromIndex, int toIndex, IntegerList output, IntegerList flags) {
        sizeRangeCheck(fromIndex, toIndex, output.size());
        sizeRangeCheck(fromIndex, toIndex, flags.size());
        rangeCheckBound(fromIndex, toIndex, size); 
        
        //flags should bin the range of 0 to (toIndex - fromIndex)
        IntegerList stencil_1 = flags.getSubList(0, toIndex - fromIndex).transform(i -> i != 0 ? 1 : 0);        
        stencil_1.add(0, 0);          
        IntegerList stencil_2 = stencil_1.transform(i -> i != 0 ? 0 : 1);
        stencil_2.set(0, 0);
        int st1 = stencil_1.prefixSum(); 
        int st2 = stencil_2.prefixSum();
        
        IntStream.range(fromIndex, toIndex)
                .parallel()
                .forEach(i->{
                    if(flags.get(i - fromIndex) != 0)                      
                        output.set(stencil_1.get(i - fromIndex), get(i));              
                });
        IntStream.range(fromIndex, toIndex)
                .parallel()
                .forEach(i->{ 
                    if(flags.get(i - fromIndex) == 0)                    
                        output.set(stencil_2.get(i - fromIndex) + st1, get(i));                    
                });
        if((st1 + st2) > output.size)
            throw new IndexOutOfBoundsException("Issue with partition");    
        
        return st1;
    }

    @Override
    protected void shiftRight(int fromIndex, int toIndex, int steps) {
        rangeCheckBound(fromIndex, toIndex, size());      
        System.arraycopy(array, fromIndex, array, fromIndex + steps, (toIndex - fromIndex) - steps);
        Arrays.fill(array, fromIndex, fromIndex + steps, 0);
    }
    
    @Override
    public int rootOffset() {
        return 0;
    }
    
    private class IntegerSubList extends IntegerList
    {      
        private final IntegerList parent;
        private final int offset;
        
        private IntegerSubList(IntegerList parent,
                int fromIndex, int toIndex) {            
            this.parent = parent;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
            this.array = null; //Assist to identify if there is an attempt to modify the array (ArrayList takes a different approach through inheritance)
            this.modCount = parent.modCount;            
        }

        @Override
        public void add(int value)
        {
            checkForComodification();
            rangeCheck(this.size - 1);            
            add(this.size, value);  
            this.modCount = parent.modCount;
        }   
        
         @Override
        public void add(int index, int[] arr)
        {
            rangeCheckForAdd(index);
            int cSize = arr.length;
            
            checkForComodification();
            parent.add(offset + index, arr);            
            this.size += cSize;     
            this.modCount = parent.modCount;
        }
        
        @Override
        public void add(int index, int e) {
            rangeCheckForAdd(index);    
            checkForComodification();
            parent.add(offset + index, e);   
            this.modCount = parent.modCount;
            this.size++;
        }
        
        @Override
        public int get(int index)
        {
            rangeCheck(index);
            checkForComodification();
            return parent.get(offset + index);
        }
        
        @Override
        public void set(int index, int e) {
            rangeCheck(index); 
            checkForComodification();
            parent.set(offset + index, e);            
        }
        
        @Override
        public void set(int index, int[] value) {       
            rangeCheck(index);         
            checkForComodification();
            parent.set(offset + index, value);            
        }
        
        @Override
        public void set(int index, IntegerList list)
        {
            rangeCheck(index);         
            checkForComodification();
            parent.set(offset + index, list);            
        }
                
        @Override
        public IntegerList getSubList(int fromIndex, int toIndex)
        {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification(); //confirm if parent is modified
            return new IntegerSubList(this, fromIndex, toIndex);
        }
        
        @Override
        public void swapElement(int index1, int index2)
        {
            rangeCheck(index1);   
            rangeCheck(index2);         
            checkForComodification();
            parent.swapElement(offset + index1, offset + index2);            
        }
               
        @Override
        public int[] trim() {              
            checkForComodification();
            return parent.trim();
        }
        
        @Override
        public int[] trimCopy()
        {
            checkForComodification();
            return parent.trimCopy(offset, offset + size());
        }
        
        @Override
        public int[] trimCopy(int fromIndex, int toIndex)
        {
            checkForComodification();
            return parent.trimCopy(offset + fromIndex, offset + toIndex);
        }
        
        
        @Override
        public void fill(int fromIndex, int toIndex, int value)
        {            
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            parent.fill(offset + fromIndex, offset + toIndex, value);
            this.modCount = parent.modCount;            
        }
        
        @Override
        public int prefixSum(int fromIndex, int toIndex)
        {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            parent.prefixSum(offset + fromIndex, offset + toIndex);
            this.modCount = parent.modCount;            
            return back();
        }
        
        @Override
        public int reduce(int fromIndex, int toIndex)
        {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            int reduce = parent.reduce(offset + fromIndex, offset + toIndex);
            this.modCount = parent.modCount;            
            return reduce;
        }
        
        @Override
        public int reduce(int fromIndex, int toIndex, int identity, IntBinaryOperator op)
        {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            int reduce = parent.reduce(offset + fromIndex, offset + toIndex, identity, op);
            this.modCount = parent.modCount;            
            return reduce;
        }
               
        @Override
        public void sort(int fromIndex, int toIndex, BiPredicate<Integer, Integer> op)
        {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            parent.sort(offset + fromIndex, offset + toIndex, op);
            this.modCount = parent.modCount;              
        }
        
        @Override
        protected void sort_pairs(int fromIndex, int toIndex, IntegerList values, BiPredicate<Integer, Integer> op)
        {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            parent.sort_pairs(offset + fromIndex, offset + toIndex, values, op);
            this.modCount = parent.modCount;              
        }
                
        @Override
        public String toString() {            
            return Arrays.toString(trimCopy());
        }
        
        @Override
        public void remove(int index) {
            rangeCheck(index);
            checkForComodification();
            parent.remove(offset + index);
            this.modCount = parent.modCount;
            size--;            
        }

        @Override
        public void remove(int fromIndex, int toIndex) {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification();            
            size -= toIndex - fromIndex;
            parent.remove(offset + fromIndex, offset + toIndex);
            this.modCount = parent.modCount;
        }
               
        @Override
        public int size() {
            checkForComodification();
            return size;
        }
        
        @Override
        public void increment(int index)
        {
            rangeCheck(index);
            checkForComodification();
            parent.increment(offset + index);
            this.modCount = parent.modCount;
        }
        
        @Override
        public void decrement(int index)
        {
            rangeCheck(index);
            checkForComodification();
            parent.decrement(offset + index);
            this.modCount = parent.modCount;
        } 
        
        @Override
        public IntegerList transform(int fromIndex, int toIndex, IntFunction<Integer> function) 
        {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification(); 
            IntegerList list = parent.transform(offset + fromIndex, offset + toIndex, function);
            this.modCount = parent.modCount;
            return list;
        }
        
        @Override
        public void transform(int fromIndex, int toIndex, IntegerList output, IntFunction<Integer> function) {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification(); 
            parent.transform(offset + fromIndex, offset + toIndex, output, function);
            this.modCount = parent.modCount;
        }
        
        @Override
        public int partition_stable(int fromIndex, int toIndex, IntegerList output, IntegerList flags) {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification(); 
            int partition = parent.partition_stable(offset + fromIndex, offset + toIndex, output, flags);
            this.modCount = parent.modCount;
            return partition;
        }
        
        @Override
        protected void shiftRight(int fromIndex, int toIndex, int steps) {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification(); 
            parent.shiftRight(offset + fromIndex, offset + toIndex, steps);
            this.modCount = parent.modCount;
            
        }
                        
        private void checkForComodification() {            
            if (parent.modCount != this.modCount)
                throw new ConcurrentModificationException("Parent array has been modified and hence this sublist is obsolete!");
        }
        
        @Override
        public int rootOffset() {
            return offset + parent.rootOffset();
        }
        
        @Override
        public boolean isSubList()
        {
            return true;
        }
    }
}
