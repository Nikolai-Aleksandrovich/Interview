package BinarySearchTree;


import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Yuyuan Huang
 * @create 2021-06-20 12:10
 */
public class BinarySearchTree<Key extends Comparable<Key>,Value>{
    private Node root;
    public int size(){
        return root.size;
    }
    public int size(Node node){
        if(node == null){
            return 0;
        }
        return node.size;
    }
    public void put(Key key,Value value){
        //查找key，找到就更新值，没有找到，插入一个新节点
        root = put(root,key,value);
    }
    private Node put(Node root, Key key, Value value) {
        //如果key存在于以root为根节点的子树中，更新这个值
        //如果不存在，将以key和value为值的新节点插入这个子树
        if (root == null) {
            return new Node(key, value, null, null, 1);
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            root.right = put(root.right,key,value);
        }else if (cmp<0){
            root.left = put(root.left,key,value);
        }else {
            root.value = value;
        }
        root.size = size(root.left)+size(root.right)+1;
        return root;


    }
    public void deleteMin(){
        deleteMin(root);
    }
    public Node deleteMin(Node root){
        if (root.left==null){
            return root.right;
        }
        root.left = deleteMin(root.left);
        root.size = size(root.left)+size(root.right)+1;
        return root;
    }
    public void deleteMax(){

    }
    public Node deleteMax(Node root){
        if (root == null){
            return null;
        }
        if (root.right==null){
            return root.left;
        }
        root.right = deleteMax(root.right);
        root.size = size(root.left)+size(root.right)+1;
        return root;

    }
    public Key select(int k){
        return select(root,k);
    }
    private Key select(Node root,int k){
        if (root == null){
            return null;
        }
        int size = size(root.left);
        if (k>size){
            return select(root.right,k-size(root.left)-1);
        }else if (k<size){
            return select(root.right,k);
        }else {
            return root.key;
        }
    }
    public int rank(Key key){
        return rank(root,key);

    }
    public int rank(Node root,Key key){
        if (root==null){
            throw new NoSuchElementException();
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            return rank(root.right,key)+1+size(root.left);
        }else if (cmp<0){
            return rank(root.left,key);
        }else {
            return size(root.left);
        }
    }
    public Key min(){
        if (root==null){
            return null;
        }
        return min(root).key;
    }
    private Node min(Node root){
        if (root.left==null){
            return root;
        }
        return min(root.left);

    }
    public Key floor(Key key){
        Node node = floor(key,root);
        if (node==null){
            return null;
        }else {
            return node.key;
        }
    }
    public Node floor(Key key,Node root){
        if (root == null){
            return null;
        }
        int cmp = key.compareTo(root.key);
        if (cmp==0){
            return root;
        }
        if(cmp<0){
            return floor(key,root.left);
        }
        Node t = floor(key,root.right);
        if (t!=null){
            return t;
        }else {
            return root;
        }

    }
//        int cmp = key.compareTo(root.key);
//        if (cmp < 0) {
//            root.left = put(root.left, key, value);
//        } else if (cmp > 0) {
//            root.right = put(root.right, key, value);
//        } else {
//            root.value = value;
////        }
//        //如果新增节点，则要递归的更新每个父节点的size值
//        root.size = size(root.left) + size(root.right) + 1;
//        return root;
//    }



//        while (root!=null){
//            int cmp = key.compareTo(root.key);
//            if (cmp>0){
//                root = root.left;
//            }else if (cmp<0){
//                root = root.right;
//            }else {
//                root.value = value;
//                return root;
//            }
//        }
//        return null;

    public Value get(Key key){
        return get(root,key);
    }
    private Value get(Node node,Key key){
        if (node == null){
            return null;
        }

        while(node!=null){
            int cmp = key.compareTo(node.key);
            if (cmp>0){
                node = node.left;
            }else if (cmp<0){
                node= node.right;
            }else {
                return node.value;
            }
        }
        return null;
    }
//    public Node min(Node root){
//        if (root == null){
//            return null;
//        }
//        if (root.left==null){
//            return root;
//        }else {
//            return min(root.left);
//        }
//    }
//    public Node deleteMin(Node root){
//        if (root==null){
//            return null;
//        }
//        if (root.left!=null){
//            root.left = deleteMin(root.left);
//        }else {
//            return root.right;
//        }
//
//        root.size = size(root.left)+size(root.right)+1;
//        return root;
//    }
    public void delete(Key key){
        delete(key,root);
    }
    private Node delete(Key key,Node root){
        if (root==null){
            return null;
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            root.right = delete(key,root.right);
        }else if (cmp<0){
            root.left = delete(key,root.left);
        }else {
            if (root.right == null){
                return root.left;
            }else if (root.left==null){
                return root.right;
            }else {
                Node t = root;
                root = min(root.right);
                root.right = deleteMin(root.right);
                root.left = t.left;
            }
        }
        root.size = size(root.left)+size(root.right)+1;
        return root;
    }
//    private Node delete(Key key,Node root){
//        if (root==null){
//            return null;
//        }
//        int cmp = key.compareTo(root.key);
//        if (cmp<0){
//            root.left = delete(key,root.left);
//        }else if (cmp>0){
//            root.right = delete(key,root.right);
//        }else {
//            if (root.right==null){
//                return root.left;
//            }else if (root.left==null){
//                return root.right;
//            }else {
//                Node t = root;
//                root = min(t.right);
//                root.left = t.left;
//            }
//        }
//        root.size = size(root.left)+size(root.right)+1;
//        return root;
//    }
    public Key max(){

    }
    private Node max(Node root){

    }
    public Iterable<Key> keys(){
        return keys(min(),)
    }
    public Iterable<Key> keys(Key lo,Key hi){
        Queue<Key> queue = new LinkedList<>();
        keys(root,queue,lo,hi);
        return queue;
    }
    private void keys(Node root,Queue<Key> queue,Key lo,Key hi){
        if (root==null){
            return;
        }
        int cmplo = lo.compareTo(root.key);
        int cmphi = hi.compareTo(root.key);
        if (cmplo<0){
            keys(root.left,queue,lo,hi);
        }
        if ((cmplo)<=0&&cmphi>=0){
            queue.offer(root.key);
        }
        if (cmphi>0){
            keys(root.right,queue,lo,hi);
        }
    }




    private class Node{
        private Key key;
        private Value value;
        private Node left;
        private Node right;
        private int size;
        public Node(Key key,Value value,Node left,Node right,int size){
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
            this.size = size;

        }
    }

}
