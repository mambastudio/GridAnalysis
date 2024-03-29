/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.utilities.list.IntegerList;
import gridanalysis.irreg.Common;
import gridanalysis.utilities.list.ObjectList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import static java.util.stream.Collectors.partitioningBy;

/**
 *
 * @author jmburu
 */
public class Test {
    public static void main(String... args)
    {
        test10();
       
        
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
        IntegerList list = new IntegerList(new int[]{1, 3, 4, 2, 5, 3, 3});
        System.out.println(list);
        list.remove(2, 7);
        System.out.println(list);
    }
    
    public static void test4()
    {
        IntegerList list = new IntegerList(new int[]{1, 3, 4, 2, 5, 3, 3});
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
        IntegerList list = new IntegerList(new int[]{1, 1, 2, 1, 1, 0});               
        list.shiftRight(1);
        list.prefixSum();
     //   System.out.println(list);
        
        int arr[] = new int[]{1, 1, 2, 1};
        int arr2[] = new int[5];
        
     //   System.arraycopy(arr, 0, arr2, 3, arr.length);
      //  System.out.println(Arrays.toString(arr2));
    }
    
   
    private static void test7() {
        ObjectList<String> stringList = new ObjectList(new String[]{"joe", "mwangi", "mburu", "nduma"});
        System.out.println(stringList);
        ObjectList<String> sub = stringList.getSublistFrom(1);
        System.out.println(sub);
        sub.sort((String a, String b)->{
            return a.length() > b.length() ? 1 : -1;
        });
        System.out.println(sub);
        System.out.println(stringList);
    }
    
    private static void test8()
    {
        ObjectList<AtomicInteger> list = new ObjectList(10, ()->new AtomicInteger(1));
        System.out.println(list);        
        list.prefix((AtomicInteger a, AtomicInteger b)->{
            return new AtomicInteger(a.get() + b.get());
        });
        System.out.println(list);
        ObjectList<AtomicInteger> list1 = list.getSublistFrom(5);
        list1.partition((AtomicInteger v) -> {
            return v.get() > 8;
        });
        
        System.out.println(list1);
        System.out.println(list);
       
    }
    
    private static void test9()
    {
        ObjectList<AtomicInteger> list = new ObjectList(10, ()->new AtomicInteger(1));
        System.out.println(list);        
        list.prefix((AtomicInteger a, AtomicInteger b)->{
            return new AtomicInteger(a.get() + b.get());
        });
        System.out.println(list);   
        ArrayList<AtomicInteger> arrList = new ArrayList();
        arrList.add(new AtomicInteger(3));
        arrList.add(new AtomicInteger(3));
        arrList.add(new AtomicInteger(3));
        arrList.add(new AtomicInteger(3));
        list.addAll(arrList);
        System.out.println(list);   
    }
    
    private static void test10()
    {
        
        IntegerList list = new IntegerList(new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3}); 
        //IntegerList flags = new IntegerList(new int[5]);
        //list.transform(5, 9, flags, i-> i == 3 ? 1 : 0);
        
        //System.out.println(list);
        //System.out.println(flags);
        
        IntegerList sublist = list.getSubList(0, 5);
        sublist.transform(sublist, i -> i == 3 ? 1 : 0);        
        System.out.println(list);
        sublist.shiftRight(1);
        System.out.println(list);
    }

    private static void test11()
    {
        IntegerList list = new IntegerList(new int[]{8, 6, 7, 5, 3, 0, 9});
       
        System.out.println(list);
        
        IntegerList keys = list.getSubList(0, 7);
        IntegerList values = new IntegerList(new int[]{0, 1, 2, 3, 4, 5, 6});
        
        System.out.println(keys);
        System.out.println(values);
        
        keys.sort_pairs(values);
        
        System.out.println(keys);
        System.out.println(values);
    }
    
    private static void test12()
    {
        int size = 10;
        IntegerList list1 = new IntegerList(new Random().ints(size, 0, 100).toArray());       
        IntegerList list2 = new IntegerList(new Random().ints(size, 0, 100).toArray());
        
        System.out.println(list1);
        System.out.println(list2);
        
        IntegerList list3 = list1.getSubListFrom(5);
        IntegerList list4 = list2.getSubListFrom(5);
        
        list3.swap(list4);
        
        System.out.println(list3);
        System.out.println(list4);
    }
    
    public static void test13()
    {
        IntegerList list1 = new IntegerList(10, 5);
        System.out.println(list1);
        IntegerList list_1 = list1.getSubListFrom(5);
        IntegerList list2 = new IntegerList(3, 2);
        System.out.println(list2);
        list_1.set(2, list2);       
        System.out.println(list1);
    }
    
    public static void test14()
    {
        IntegerList list = new IntegerList(new int[]{8, 6, 7, 5, 3, 0, 9});        
        list.getSubListFrom(3).shiftRight(1);
        System.out.println(list);
    }
}
