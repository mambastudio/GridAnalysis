/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

/**
 *
 * @author user
 */
public class MergePair  {
    public int first;
    public int second;
    
    public MergePair(int first, int second)
    {
        this.first = first; this.second = second;
    }
    
    // Overriding the toString() method
    // to print the pair
    @Override
    public String toString()
    {
        return "(" + first + ", " + second + ")";
    }
}
