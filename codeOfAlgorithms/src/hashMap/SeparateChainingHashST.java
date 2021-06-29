package hashMap;

import java.util.Iterator;

/**
 * @author Yuyuan Huang
 * @create 2021-06-29 15:05
 */
public class SeparateChainingHashST <Key,Value>{
    //total Number of Entry
    private int N;
    //链表的数量
    private int M;
    //array which you store Entry
    private SequentialSearchST<Key,Value>[] st;
    public SeparateChainingHashST(){
        this(997);
    }
    public SeparateChainingHashST(int M){
        //create M linkedList
        this.M = M;
        //实例化st,因为java不允许有泛型的数组出现
        st = (SequentialSearchST<Key, Value>[]) new SequentialSearchST[M];
        //对每一个索引的位置，都创建一个实例
        for (int i = 0; i < M; i++) {
            st[i] = new SequentialSearchST<>(null);
        }
    }
    private int hash(Key key){
        return (key.hashCode()&0x7fffffff)%M;
    }
    public Value get(Key key){
        return (Value)st[hash(key)].get(key);
    }
    public void put(Key key,Value value){
        st[hash(key)].put(key,value);
    }
    public Iterable<Key> keys(){

    }
    private class SequentialSearchST<Key extends Iterator,Value> implements Iterable<Key>{
        private int size;
        private Node node;
        public SequentialSearchST(Node node){
            this.node = node;
        }
        public void delete(Key key){
            if (node == null){
                return;
            }

            Node prev = node;
            if (prev.key == key){
                node = prev.next;
                return;
            }
            while (prev!=null){
                Node next = prev.next;
                if (next!=null&&next.key == key){
                    prev.next = next.next;
                }else {
                    return;
                }
                prev = prev.next;
            }
        }
        public Value get(Key key){
            Node temp = node;
            while(temp!=null){
                if (temp.key==key){
                    return temp.value;
                }
                temp = temp.next;
            }
            return null;
        }
        public void put(Key key,Value value){
            Node temp = node;
            while(temp!=null){
                if (temp.key == key){
                    temp.value = value;
                    return;
                }
                temp = temp.next;
            }
            node = new Node(key,value,node);
        }

        @Override
        public Iterator<Key> iterator() {
            return ;
        }

        private class
        private class Node{
            Key key;
            Value value;
            Node next;
            public Node(Key key,Value value,Node next){
                this.key = key;
                this.value = value;
                this.next = next;
            }
        }
    }
}
