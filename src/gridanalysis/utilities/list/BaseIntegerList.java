/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities.list;

/**
 *
 * @author user
 * @param <BaseIntList>
 */
public interface BaseIntegerList<BaseIntList extends BaseIntegerList> 
{
    public void add(int value);   
    public void add(int index, int value);
    public void add(int index, int[] value);
    
    public int get(int index);
    
    public void set(int index, int value);    
    public void set(int index, int[] value);  
    
    public void set(BaseIntList list);        
    public void set(int index, BaseIntList list);  
    
    default BaseIntList copyTo(BaseIntList list)
    {
        //range checks will be done in the implementation of set
        list.set(this); 
        return list;
    }
    
    default BaseIntList copyTo(int n, BaseIntList list)
    {
        list.set(0, this.trimCopy(0, n));
        return list;
    }
    
    public void increment(int index);
    public void decrement(int index);
    
    public BaseIntList getSubListFrom(int fromIndex);    
    public BaseIntList getSubList(int fromIndex, int toIndex);    
    
    public void remove(int index);     
    public void remove(int fromIndex, int toIndex);
    
    public int size();   
    
    //traverse up the list hierarchy of parents to root to get the actual offset from the main array
    public int rootOffset();    
    //traverse up the list hierarchy of parents to root to get the main array
    public int[] trim();  
    
    public int[] trimCopy();
    public int[] trimCopy(int fromIndex, int toIndex); 
        
    public void resize(int size);
    public void resize(int size, int value);
    
    public void swapElement(int index1, int index2);
    public void swap(BaseIntList list);
    
    default boolean isSubList() {return false;}
}
