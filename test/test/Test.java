/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.irreg.Common;
import gridanalysis.irreg.IntList;
import gridanalysis.utilities.Utility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import static java.util.stream.Collectors.partitioningBy;

/**
 *
 * @author jmburu
 */
public class Test {
    public static void main(String... args)
    {
        test6();
       
        
    }
    
    public static void test1()
    {
        int pop_count[] = { 0, 1, 1, 2,
                            1, 2, 2, 3,
                            1, 2, 2, 3,
                            2, 3, 3, 4};
        
        int flag = Integer.parseInt("01111", 2); 
        
        System.out.println(pop_count[flag]);
    }
    
    public static void test2()
    {
        ArrayList<Integer> intList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        int pivot = partition(intList, (Integer i) -> i % 2 == 0);
        
        System.out.println(intList);
        System.out.println(pivot);
    }
    
    private static<T> int partition(List<T> list, Predicate<T> predicate)
    {
        Map<Boolean, List<T>> result = list.stream().collect(partitioningBy(predicate));
        List<T> trueList = result.get(true);
        List<T> falseList = result.get(false);
        int index = trueList.size();
        
        list.clear();
        list.addAll(trueList);
        list.addAll(falseList);
        
        return index;
    }
    
    public static void test3()
    {
        IntList list = new IntList(new int[]{1, 3, 4, 2, 5, 3, 3});
        System.out.println(list);
        list.remove(2, 7);
        System.out.println(list);
    }
    
    public static void test4()
    {
        IntList list = new IntList(new int[]{1, 3, 4, 2, 5, 3, 3});
        System.out.println(list);
        list.resize(31);
        System.out.println(list);
    }
    
    public static void test5()
    {
        ArrayList<Integer> list = new ArrayList(Arrays.asList(new Integer[]{1, 3, 4, 2, 5, 3, 3}));
        System.out.println(list);
        Common.resize(list, 3, ()->0);
        System.out.println(list);
        
        System.out.println(19&~7);
    }

    private static void test6() {
         IntegerList list = new IntegerList(1, 2, 3, 4, 5, 6);
         IntegerList subList1 = list.getSubList(1, 3);
         System.out.println(list);
         System.out.println(subList1);
         
         subList1.add(32);
         System.out.println(subList1);
         
         IntegerList subList2 = subList1.getSubList(0, 1);
         System.out.println(subList2);         
         subList2.add(200);         
         System.out.println(subList2);         
         
         System.out.println(list);
    }
}
