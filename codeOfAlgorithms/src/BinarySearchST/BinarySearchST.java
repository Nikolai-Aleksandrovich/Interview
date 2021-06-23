package BinarySearchST;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Yuyuan Huang
 * @create 2021-06-19 21:59
 */
public class BinarySearchST<Key extends Comparable<Key>,Value> implements Iterable<BinarySearchST<Key, Value>.Entry> {
    private Key[] keys;
    private Value[] values;
    private int N;
    public BinarySearchST(int capacity){
        keys = (Key[])new Comparable[capacity];
        values = (Value[]) new Object[capacity];
    }
    public int size(){
        return N;
    }
    public boolean isEmpty(){
        return N == 0;
    }
    public Key min(){
        return keys[0];
    }
    public Key max(){
        return keys[N-1];
    }
    public Key select(int k){
        return keys[k];
    }
    public Key ceiling(Key key){
        int i = rank(key);
        return keys[i];
    }
    public void delete(Key key){
        if (!contains(key)){
            return;
        }
        int i = rank(key);
        if (i>=0&&i<N){
            if (N== keys.length/4){
                resize( values.length/2);
            }
            for (int j = N; j > i; j--) {
                keys[j+1] = keys[j];
                values[j+1] = values[j];
            }
        }
    }

    public void put(Key key,Value value){
        int i = rank(key);
        if (i<N&&keys[i].compareTo(key)==0){
            values[i] = value;
        }
        if (N==keys.length){
            resize(2* values.length);
        }
        for (int j = N; j > i; j--) {
            keys[j] = keys[j-1];
            values[j] = values[j-1];
        }
        keys[i] = key;
        values[i] = value;
        N++;
    }
    public void resize(int maxSize){
        Key[] tempKeys = (Key[])new Comparable[maxSize];
        Value[] tempValue = (Value[])new Object[maxSize];
        for (int i = 0; i < N; i++) {
            tempKeys[i] = keys[i];
            tempValue[i] = values[i];
        }
        keys = tempKeys;
        values = tempValue;
    }
    public Value get(Key key){
        if (!contains(key)){
            return null;
        }
        int i = rank(key);
        return values[i];
    }
    public boolean contains(Key key){
        for (int i = 0; i < N; i++) {
            if (keys[i].compareTo(key) == 0 ){
                return true;
            }
        }
        return false;
    }
    private int rank(Key key){
        if (!contains(key)){
            throw new NoSuchElementException();
        }
        int lo = 0;
        int hi = keys.length;
        while (lo<hi){
            int mid = (hi+lo)/2;
            if (keys[mid].compareTo(key)>0){
                hi = mid-1;
            }else if (keys[mid].compareTo(key)<0){
                lo=mid+1;
            }else {
                return mid;
            }
        }
        return lo;

    }
    private int rank(Key key,int lo,int hi){
        if (lo>hi){
            return lo;
        }
        int mid = (lo+hi)/2;
        if (keys[mid].compareTo(key)>0){
            return rank(key,lo,mid-1);
        }else if (keys[mid].compareTo(key)<0){
            return rank(key,mid+1,hi);
        }else {
            return mid;
        }

    }
    public Iterable<Key> keys(Key lo,Key hi){
        Queue<Key> q = new LinkedList<>();
        for (int i = rank(lo); i < rank(hi); i++) {
            q.offer(keys[i]);
        }
        if (contains(hi)){
            q.offer(keys[rank(hi)]);
        }
        return q;
    }
    @Override
    public Iterator<Entry> iterator(){
        return new InnerIterator();
    }
    private class InnerIterator implements Iterator<Entry>{
        private int i = N;
        @Override
        public boolean hasNext() {
            if (i+1<=N){
                i++;
                return true;
            }else{
                return false;
            }
        }

        @Override
        public Entry next() {
            if (i+1<=N) {
                return new Entry(keys[++i], values[++i]);
            }else {
                return null;
            }
        }

    }
    protected class Entry{
        private Key key;
        private Value value;
        public Entry(Key key,Value value){
            this.key = key;
            this.value = value;
        }
    }
}
