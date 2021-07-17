package FixedCapicityStackOfStrings;

/**
 * @author Yuyuan Huang
 * @create 2021-06-29 16:55
 */
public class LinearProbingHashST <Key,Value>{
    //键值总数
    private int N;
    //数组总大小
    private int M = 16;
    private Key[] keys;
    private Value[] values;
    public LinearProbingHashST(int length){
        keys = (Key[])new Object[M];
        values = (Value[])new Object[M];
    }

    private int hash(Key key){
        return (key.hashCode()&0x7fffffff)%M;
    }
    public boolean contains(Key key){
        int hash = hash(key);
        for (int i = hash; keys[i]!=null; i=(i+1)%M) {
            if (keys[i].equals(key)){
                return true;
            }
        }
        return false;
    }
//    private void resize(int length){
//        Key[] keys1 = (Key[]) new Object[length];
//        Value[] values1 = (Value[]) new Object[length];
//        for (int i = 0; i < M; i++) {
//            keys1[i] = keys[i];
//            values1[i] = values[i];
//        }
//    }
    private void put(Key key, Value value){
        if (N>M/2){
            resize(2* keys.length);
        }
        int i;
        for (i = hash(key); keys[i]!=null ; i=(i+1)%M) {
            if (keys[i].equals(key)){
                values[i] = value;
                return;
            }
        }
        keys[i] = key;
        values[i] = value;
        N++;
    }
    private Value get(Key key){
        int hash = hash(key);
        for (int i = hash; keys[i]!=null; i = (i+1)%M) {
            if (keys[i].equals(key)){
                return values[i];
            }
        }
        return null;
    }
    public void delete(Key key){
        if (!contains(key)){
            return;
        }
        int i = hash(key);
        while(!key.equals(keys[i])){
            i = (i+1)%M;

        }
        keys[i] = null;
        values[i] =null;
        i = (i+1)%M;
        while (keys[i]!=null){
            Key keysToRedo = keys[i];
            Value valToRedo = values[i];
            keys[i] = null;
            values[i] = null;
            N--;
            put(keysToRedo,valToRedo);
            i = (i+1)%M;

        }
        N--;
        if (N>0&&N==N/8){
            resize(M/2);
        }

    }
    private void resize(int length){
        LinearProbingHashST<Key,Value> t;
        t = new LinearProbingHashST<>(length);
        for (int i = 0; i < M; i++) {
            if (keys[i]!=null){
                t.put(keys[i],values[i]);
            }
        }
        keys = t.keys;
        values = t.values;
        M=t.M;
    }


}
