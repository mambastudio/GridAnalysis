/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities.list;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author jmburu
 * @param <T>
 * @param <O>
 */
public abstract class ObjectListAbstract<T, O extends ObjectListAbstract<T, O>> {
    protected static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    protected Object[] array;
    protected int size;  
    protected int modCount = 0;
        
    public abstract void add(T value);   
    public abstract void add(int index, T value);
    public abstract void add(int index, T[] value);
    public abstract T get(int index);
    public abstract void set(int index, T value);
    public abstract void set(int index, T[] value);
    public abstract T[] trim();
    public abstract<S extends O> S getSubList(int fromIndex, int toIndex);
    public <S extends O> S getSublistFrom(int fromIndex){return getSubList(fromIndex, size());}
    public abstract void remove(int index);     
    public abstract void remove(int fromIndex, int toIndex);
    public abstract void removeIf(Predicate<T> predicate);
    public abstract int size();    
    public abstract int end();  
    public abstract T back();      
    public abstract T[] toArray();
    public abstract void clear();
    public abstract void resize(int size);
    public abstract void resize(int size, Supplier<T> supply);
    
    public abstract void swap(ObjectListAbstract<T, O> list);
    public abstract int find(int first, int end, T value);       
    
    public ListIterator<T> listIterator(final int index) {
        rangeCheckForAdd(index);

        return new ListItr(index);
    }
    
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
    
    private class Itr implements Iterator<T> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         */
        int lastRet = -1;

        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public T next() {
            checkForComodification();
            try {
                int i = cursor;
                T next = get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ObjectListAbstract.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListItr extends Itr implements ListIterator<T> {
        ListItr(int index) {
            cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public T previous() {
            checkForComodification();
            try {
                int i = cursor - 1;
                T previous = get(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor-1;
        }

        @Override
        public void set(T e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ObjectListAbstract.this.set(lastRet, e);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void add(T e) {
            checkForComodification();

            try {
                int i = cursor;
                ObjectListAbstract.this.add(i, e);
                lastRet = -1;
                cursor = i + 1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

}
