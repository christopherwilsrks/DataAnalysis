package com.recommender.utils;

import java.util.Comparator;

/**
 * @ClassName ArrayIndexComparator
 * @Description
 * @Author christ
 * @Date 2019/4/28 22:39
 **/
public class ArrayIndexComparator implements Comparator<Integer> {

    private final Double[] array;

    public ArrayIndexComparator(Double[] array)
    {
        this.array = array;
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i; // Autoboxing
        }
        return indexes;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        return array[o2].compareTo(array[o1]);
    }
}
