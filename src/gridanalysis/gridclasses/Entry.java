/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

/**
 *
 * @author user
 */
public class Entry {
    public int log_dim;    ///< Logarithm of the dimensions of the entry (0 for leaves)
    public int begin;      ///< Next entry index (cell index for leaves)
    
    public Entry(){}
    
    public Entry(int log_dim, int begin)
    {
        this.log_dim = log_dim;
        this.begin = begin;
    }
    
    @Override
    public String toString()
    {
        return "log_dim " +log_dim+ "\n"
             + " begin " +begin;
    }
}
