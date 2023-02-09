/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

/**
 *
 * @author jmburu
 */
public class Test2 {
    public static void main(String... args)
    {
        int v = Integer.MAX_VALUE;
       
        System.out.println(log2(v));
        
    }
    
    public static int log2(int n)
    {
        if(n <= 0) throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }
}
