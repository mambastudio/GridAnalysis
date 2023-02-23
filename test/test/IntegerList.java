/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Arrays;

/**
 *
 * @author user
 */
public class IntegerList extends IntListAbstract<IntegerList> {
    
    public IntegerList()
    {
        this.array = new int[10];       
        this.size = 0;
    }
    
    public IntegerList(int... array)
    {
        if(array == null)
            throw new NullPointerException("array is null");
        this.array = array;        
        this.size = array.length;
    }

    @Override
    public void add(int value) {
        ensureCapacity(size + 1);
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
    public int[] trim() {
        if(size < array.length)
            array = Arrays.copyOfRange(array, 0, size);        
        return array;
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
    public int remove(int index) {
        rangeCheck(index);
        int oldValue = array[index];
        
        int numMoved = size - (index) - 1;
        if (numMoved > 0)
            System.arraycopy(array, index + 1, array, index,
                             numMoved);
        size--;
        return oldValue;
    }

    @Override
    public int[] remove(int fromIndex, int toIndex) {
        rangeCheckBound(fromIndex, toIndex, size);
        int removeSize = toIndex - fromIndex;
        int[] arr = new int[removeSize];
        
        for(int i = 0; i<removeSize; i++)
            arr[i] = remove(fromIndex);
        
        return arr;
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
        ensureCapacity(size + 1);  
        System.arraycopy(array, index, array, index + 1,
                         size - index);
        array[index] = value;
        size++;
    }

    @Override
    public void add(int index, int[] value) {
        for(int i = 0; i<value.length; i++)
            add(index+i, value[i]);
    }

    @Override
    public String toString() {
        return Arrays.toString(trim());
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
        }

        @Override
        public void add(int value)
        {
            rangeCheck(this.size - 1);
            add(this.size, value);            
        }   
        
        @Override
        public void add(int index, int e) {
            rangeCheckForAdd(index);            
            parent.add(offset + index, e);           
            this.size++;
        }
        
        @Override
        public int get(int index)
        {
            rangeCheck(index);
            return parent.get(offset + index);
        }
                
        @Override
        public IntegerList getSubList(int fromIndex, int toIndex)
        {
            rangeCheckBound(fromIndex, toIndex, size);
            return new IntegerSubList(this, fromIndex, toIndex);
        }
        
        /*
        @Override
        public void set(int index, int value)
        {
            
        }
        
        @Override
        public int[] trim()
        {
            
        }
        
        @Override
        public void increment(int index)
        {
            
        }
        
        @Override
        public void decrement(int index)
        {
            
        }
        
        
        
        @Override
        public int remove(int index)
        {
            
        }
        
        @Override
        public int[] remove(int fromIndex, int toIndex)
        {
            
        }
        
        @Override
        public int size()
        {
            
        }
        
        @Override
        public int end()
        {
            
        }
        
        @Override
        public int back()
        {
            
        }
        
        @Override
        public void add(int index, int value)
        {
            
        }
        
        @Override
        public void add(int index, int[] value)
        {
            
        }
        */
        
        @Override
        public int[] trim() {
            int[] arr = new int[size];
            for(int i = 0; i<size; i++)
                arr[i] = get(i);
            return arr;
        }
    }
}
