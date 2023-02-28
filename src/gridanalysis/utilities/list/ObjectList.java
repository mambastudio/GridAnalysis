/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities.list;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static java.util.stream.Collectors.partitioningBy;

/**
 *
 * @author user
 * @param <T>
 */
public class ObjectList<T> extends ObjectListAbstract<T, ObjectList<T>> {
    public ObjectList()
    {
        this.array = new Object[10];       
        this.size = 0;
    }
    
    public ObjectList(T[] array)
    {
        if(array == null)
            throw new NullPointerException("array is null");
        this.array = array;        
        this.size = array.length;
    }
    
    public ObjectList(int size, Supplier<T> supplier)
    {
        if(size < 1)
            throw new IndexOutOfBoundsException("size is less than 1");
        this.array = new Object[size];    
        fill(0, array.length, supplier);
        this.size = array.length;
    }

    @Override
    public void add(T value) {        
        ensureCapacity(size + 1); //increments modCount
        array[size] = value;    
        size++;       
    }

    @Override
    public T get(int index) {
        rangeCheck(index);
        return (T) this.array[index];
    }

    @Override
    public void set(int index, T value) {
        rangeCheck(index);
        this.array[index] = value;
    }
    
    @Override
    public void set(int index, T[] value) {       
        rangeCheck(index);   
        System.arraycopy(value, 0, array, index, value.length);        
    }
   
    @Override
    public ObjectList<T> getSubList(int fromIndex, int toIndex) {
        rangeCheckBound(fromIndex, toIndex, size);
        return new ObjectSublist(this, fromIndex, toIndex);
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
    public void removeIf(Predicate<T> predicate) {
        ListIterator<T> iterator = listIterator(0);
        while(iterator.hasNext())
        {
            T t = iterator.next();
            if(predicate.test(t))
                iterator.remove();
        }            
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
    public T back() {
        return get(end() - 1);
    }

    @Override
    public void add(int index, T value) {
        rangeCheckForAdd(index);        
        ensureCapacity(size + 1);  //add and remove have to modify the modcount
        System.arraycopy(array, index, array, index + 1,
                         size - index);
        array[index] = value;
        size++;       
    }
 
    @Override
    public void add(int index, T[] arr) {
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
    
    public void addAll(List<T> list)
    {
        add(size, (T[])list.toArray());
    }

    @Override
    public T[] toArray() {    
        return Arrays.copyOfRange((T[]) array, 0, size);   
    }
    
    public void sort(Comparator<? super T> c)
    {
        sort(0, size, c);
    }
    
    public void sort(int fromIndex, int toIndex, Comparator<? super T> c) {
        rangeCheckBound(fromIndex, fromIndex, size);
        final int expectedModCount = modCount;
        Arrays.sort((T[]) array, fromIndex, toIndex, c);
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
    }
    
    public void parallelPrefix(BinaryOperator<T> op)
    {
        parallelPrefix(0, size, op);
    }
    
    public void parallelPrefix(int fromIndex, int toIndex, BinaryOperator<T> op)
    {
        rangeCheckBound(fromIndex, fromIndex, size);
        final int expectedModCount = modCount;
        Arrays.parallelPrefix((T[])array, fromIndex, toIndex, op);
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
    }
    
    public int partition(Predicate<T> predicate)
    {
        return partition(0, size, predicate);
    }
    
    public int partition(int fromIndex, int toIndex, Predicate<T> predicate)
    {
        rangeCheckBound(fromIndex, fromIndex, size);
        final int expectedModCount = modCount; //imagine partitioning the whole array - this might cause sublist misbehaviour
        Map<Boolean, List<T>> result = Arrays.stream((T[])array, fromIndex, toIndex).collect(partitioningBy(predicate));
        List<T> trueList = result.get(true);
        List<T> falseList = result.get(false);
        int index = trueList.size();        
        this.set(fromIndex, (T[]) trueList.toArray());
        if(index < toIndex)
            this.set(fromIndex + index, (T[]) falseList.toArray());
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
        
        return index;
    }
    
    public void fill(Supplier<T> supplier)
    {
        fill(0, size, supplier);
    }
    
    public void fill(int fromIndex, int toIndex, Supplier<T> supplier)
    {
        rangeCheckBound(fromIndex, fromIndex, size);
        final int expectedModCount = modCount;
        for(int i = fromIndex; i<toIndex; i++)
            array[i] = supplier.get();        
        if (modCount != expectedModCount) {
          throw new ConcurrentModificationException();
        }
        modCount++;
    }
    
    @Override
    public void forEach(Consumer<? super T> action) {
        forEach(0, size, action);
    }
    
    public void forEach(int fromIndex, int toIndex, Consumer<? super T> action) {
        if(action == null)
            throw new NullPointerException("action is null");
        final int expectedModCount = modCount;
        for (int i = fromIndex; modCount == expectedModCount && i < toIndex; i++) {
            action.accept(get(i));
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
    
    @Override
    public T[] trim() {       
        if(size < array.length)        
            array = Arrays.copyOfRange(array, 0, size);        
        return (T[]) array;
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
            Object[] arrayNew = new Object[size - size()];
            for(int i = 0; i<arrayNew.length; i++)
                arrayNew[i] = null;
            add(size(), (T[]) arrayNew);
        }
    }

    @Override
    public void resize(int size, Supplier<T> supplier) {
        rangeAboveZero(size);               
        if(size < size())        
            remove(size, size());        
        else
        {
            Object[] arrayNew = new Object[size - size()];
            for(int i = 0; i<arrayNew.length; i++)
                arrayNew[i] = supplier.get();
            add(size(), (T[]) arrayNew);
        }
    }

    
    @Override
    public void swap(ObjectListAbstract<T, ObjectList<T>> list)
    {
        T[] temp = toArray();
        
        clear();
        add(0, list.toArray());
        
        list.clear();
        list.add(0, temp);        
    }
    
    @Override
    public int find(int first, int end, T value)
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

    
    private class ObjectSublist<T> extends ObjectList<T>
    {      
        private final ObjectList<T> parent;
        private final int offset;
        
        private ObjectSublist(ObjectList<T> parent,
                int fromIndex, int toIndex) {            
            this.parent = parent;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
            this.array = null; //Assist to identify if there is an attempt to modify the array (ArrayList takes a different approach through inheritance)
            this.modCount = parent.modCount;            
        }

        @Override
        public void add(T value)
        {
            checkForComodification();
            rangeCheck(this.size - 1);
            add(this.size, value);            
        }   
        
        @Override
        public void addAll(List<T> list)
        {
            checkForComodification();
            rangeCheck(this.size - 1);
            add(this.size, (T[])list.toArray());      
            this.modCount = parent.modCount;
        }
        
         @Override
        public void add(int index, T[] arr)
        {
            rangeCheckForAdd(index);
            int cSize = arr.length;
            
            checkForComodification();
            parent.add(offset + index, arr);
            this.modCount = parent.modCount;
            this.size += cSize;    
            this.modCount = parent.modCount;
        }
        
        @Override
        public void add(int index, T e) {
            rangeCheckForAdd(index);    
            checkForComodification();
            parent.add(offset + index, e);   
            this.modCount = parent.modCount;
            this.size++;
        }
        
        @Override
        public T get(int index)
        {
            rangeCheck(index);
            checkForComodification();
            return parent.get(offset + index);
        }
        
        @Override
        public void set(int index, T e) {
            rangeCheck(index);   
            checkForComodification();
            parent.set(offset + index, e);
        }
        
        @Override
        public void set(int index, T[] value) {       
            rangeCheck(index);   
            checkForComodification();
            parent.set(offset + index, value);
        }
                
        @Override
        public ObjectSublist<T> getSubList(int fromIndex, int toIndex)
        {
            rangeCheckBound(fromIndex, toIndex, size);
            checkForComodification(); //confirm if parent is modified
            return new ObjectSublist(this, fromIndex, toIndex);
        }
        
        @Override
        public T[] trim() {              
            checkForComodification();
            Object[] arr = new Object[parent.size];
            if(size < arr.length)        
            {                
                arr = Arrays.copyOfRange((T[]) parent.trim(), offset, offset + size);
            }        
            return (T[]) arr;
        }
        
        @Override
        public T[] toArray()
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
        public T back() {
            checkForComodification();
            return get(end() - 1);
        }
        
        @Override
        public void sort(Comparator<? super T> c)
        {
            sort(0, size, c);
        }

        @Override
        public void sort(int fromIndex, int toIndex, Comparator<? super T> c) {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            parent.sort(offset + fromIndex, offset + toIndex, c);
            this.modCount = parent.modCount;
        }
        
        @Override
        public void parallelPrefix(BinaryOperator<T> op)
        {
            parallelPrefix(0, size, op);
        }

        @Override
        public void parallelPrefix(int fromIndex, int toIndex, BinaryOperator<T> op) {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            parent.parallelPrefix(offset + fromIndex, offset + toIndex, op);
            this.modCount = parent.modCount;
        }
        
        @Override
        public int partition(Predicate<T> predicate)
        {
            return partition(0, size, predicate);
        }

        @Override
        public int partition(int fromIndex, int toIndex, Predicate<T> predicate)
        {
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            int index = parent.partition(offset + fromIndex, offset + toIndex, predicate);
            this.modCount = parent.modCount;
            return index;
        }
        
        @Override
        public void fill(Supplier<T> supplier)
        {
            fill(0, size, supplier);
        }

        @Override
        public void fill(int fromIndex, int toIndex, Supplier<T> supplier)
        {            
            rangeCheckBound(fromIndex, fromIndex, size);
            checkForComodification();
            parent.fill(offset + fromIndex, offset + toIndex, supplier);
            this.modCount = parent.modCount;            
        }
                        
        private void checkForComodification() {            
            if (parent.modCount != this.modCount)
                throw new ConcurrentModificationException("Parent array has been modified and hence this sublist is obsolete!");
        }
    }
    
}
