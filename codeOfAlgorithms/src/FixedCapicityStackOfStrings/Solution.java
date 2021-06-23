package FixedCapicityStackOfStrings;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Yuyuan Huang
 * @create 2021-06-18 20:29
 */
public class Solution {
    public static void main(String[] args) {
        FixedCapacityStackOfStrings fixedCapacityStackOfStrings;
        fixedCapacityStackOfStrings = new FixedCapacityStackOfStrings(100);
        for (int i = 0; i < 100; i++) {
            fixedCapacityStackOfStrings.add("a");
        }
        System.out.println(fixedCapacityStackOfStrings.toString());

    }

}

class FixedCapacityStackOfStrings<Items> implements Iterable<Items>{
    //content
    private Items[] a;
    //size
    private int N;
    //constructor
    public FixedCapacityStackOfStrings(int length){
        a = (Items[])new Object[length];
    }
    //isEmpty
    public boolean IsEmpty(){
        return N==0;
    }
    //add
    public void add(Items item){
        if (N==a.length){
            resize(2*a.length);
        }
        a[N++] = item;
    }
    //delete
    public Items delete(){
        Items items = a[--N];
        a[N] = null;
        if (N>0&&N==a.length/4){
            resize(a.length/2);
        }
        return items;
    }
    public int size(){
        return N;
    }
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < N; i++) {
            stringBuilder.append(a[i]);
        }
        return stringBuilder.toString();
    }
    //resize
    public void resize(int max){
        Items[] temp = (Items[])new Object[max];
        for (int i = 0; i < N; i++) {
            temp[i] = a[i];
        }
        a = temp;
    }


    @Override
        public Iterator<Items> iterator() {
        return new ReverseArrayIterator();
    }
    private class ReverseArrayIterator implements Iterator<Items>{
        private int i = N;
        @Override
        public boolean hasNext(){
            return i>0;
        }
        @Override
        public Items next(){
            if (i==0){
                throw new NoSuchElementException();
            }
            return a[--i];
        }
        public void remove(){
            throw new UnsupportedOperationException();
        }

    }
}
