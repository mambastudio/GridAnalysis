/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.utilities;

/**
 *
 * @author user
 * @param <T>
 */
public class ObjHolder<T> {
    private T t;
    
    public ObjHolder(T t)
    {
        this.t = t;
    }
    
    public ObjHolder(ObjHolder<T> objHolder)
    {
        this(objHolder.get());
    }
    
    public T get()
    {
        return t;
    }
    
    public void set(T t)
    {
        this.t = t;
    }
    
    public void swap(ObjHolder<T> tHolder)
    {
        T temp = t;
        this.t = tHolder.get();
        tHolder.set(temp);
    }
}
