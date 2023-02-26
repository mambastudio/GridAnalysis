/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import java.util.Arrays;
import java.util.ConcurrentModificationException;

/**
 *
 * @author user
 * 
 * Simple List that behaves like an ArrayList but for integer primitive
 * All codes here are based on jdk 1.8 ArrayList implementation
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
        rangeCheck(index);   
        System.arraycopy(value, index, array, index, size);        
    }

    @Override
    public void increment(int index) {
        rangeCheck(index);
        array[index]++;
    }

    @Override
    public void decrement(int index) {
        rangeCheck(index);
        array[index]--;
    }

    @Override
    public IntegerList getSubList(int fromIndex, int toIndex) {
        rangeCheckBound(fromIndex, toIndex, size);
        return new IntegerSubList(this, fromIndex, toIndex);
    }
    
    @Override
    public void remove(int index) {
        rangeCheck(index);
        
        modCount++; //add and remove have to modify the modcount       
        int numMoved = size - (index) - 1;
        if (numMoved > 0)
            System.arraycopy(array, index + 1, array, index,
                             numMoved);
        size--;
    }
   
    @Override
    public void remove(int fromIndex, int toIndex) {
        rangeCheckBound(fromIndex, toIndex, size);
        
        modCount++; //add and remove have to modify the modcount  
        int numMoved = size - toIndex;
        System.arraycopy(array, toIndex, array, fromIndex,
                         numMoved);
        int newSize = size - (toIndex-fromIndex);
        size = newSize;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int end() {
        return size();
    }

    @Override
    public int back() {
        return get(end() - 1);
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
    public int[] toArray() {    
        return Arrays.copyOfRange(array, 0, size);   
    }
    
    @Override
    public int[] trim() {       
        if(size < array.length)        
            array = Arrays.copyOfRange(array, 0, size);        
        return array;
    }
    
    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }

    @Override
    public void clear() {
        remove(0, size);
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
    public IntegerList prefixSum() {
        int[] arr = toArray();
        Arrays.parallelPrefix(arr, (x, y) -> x + y);
        set(0, arr);
        return new IntegerList(arr);
    }
    
    @Override
    public void swap(IntegerList list)
    {
        int[] temp = toArray();
        
        clear();
        add(0, list.toArray());
        
        list.clear();
        list.add(0, temp);        
    }
    
    @Override
    public int find(int first, int end, int value)
    {
        int flags_it = -1;
        for (int i = 0; i < size(); i++) {
            if (get(i) == value) {
                flags_it = i;
                break;
            }
        }
        return flags_it;
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
        }   
        
         @Override
        public void add(int index, int[] arr)
        {
            rangeCheckForAdd(index);
            int cSize = arr.length;
            
            checkForComodification();
            parent.add(offset + index, arr);
            this.modCount = parent.modCount;
            this.size += cSize;            
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
        public IntegerList getSubList(int fromIndex, int toIndex)
        {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification(); //confirm if parent is modified
            return new IntegerSubList(this, fromIndex, toIndex);
        }
        
        @Override
        public int[] trim() {              
            checkForComodification();
            int[] arr = new int[parent.size];
            if(size < arr.length)        
            {                
                arr = Arrays.copyOfRange(parent.trim(), offset, offset + size);
            }        
            return arr;
        }
        
        @Override
        public int[] toArray()
        {
            checkForComodification();
            return Arrays.copyOfRange(parent.toArray(), offset, offset + size);
        }
        
        @Override
        public String toString() {            
            return Arrays.toString(toArray());
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
        public int end() {
            checkForComodification();
            return size();
        }

        @Override
        public int back() {
            checkForComodification();
            return get(end() - 1);
        }
        
        @Override
        public void increment(int index)
        {
            rangeCheck(index);
            checkForComodification();
            parent.increment(offset + index);
        }
        
        @Override
        public void decrement(int index)
        {
            rangeCheck(index);
            checkForComodification();
            parent.decrement(offset + index);
        } 
                
        private void checkForComodification() {            
            if (parent.modCount != this.modCount)
                throw new ConcurrentModificationException("Parent array has been modified and hence this sublist is obsolete!");
        }
    }
}
