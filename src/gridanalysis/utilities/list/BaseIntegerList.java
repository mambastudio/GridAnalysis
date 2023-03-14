/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities.list;

/**
 *
 * @author user
 */
public interface BaseIntegerList 
{
    public void add(int value);   
    public void add(int index, int value);
    public void add(int index, int[] value);
    
    public int get(int index);
    
    public void set(int index, int value);    
    public void set(int index, int[] value);    
    
    public void increment(int index);
    public void decrement(int index);
    
    public BaseIntegerList getSubListFrom(int fromIndex);    
    public BaseIntegerList getSubList(int fromIndex, int toIndex);    
    
    public void remove(int index);     
    public void remove(int fromIndex, int toIndex);
    
    public int size();   
    
    //scale up the list hierarchy of parents to root to get the offset from the main array
    public int rootOffset();
    
    public int[] trim();  
    public int[] trimCopy();
    public int[] trimCopy(int fromIndex, int toIndex);
        
    public void resize(int size, int value);
    
    public void swapElement(int index1, int index2);
    public void swap(BaseIntegerList list);
}
