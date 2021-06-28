package RedAndBlackTree;

import org.w3c.dom.Node;

/**
 * @author Yuyuan Huang
 * @create 2021-06-26 20:59
 */
public class MyOwnRBT<Key extends Comparable<Key>,Value> {
    private static final boolean RED = true;
    private static final boolean BLACK = false;
    private Node root;
    public MyOwnRBT(Key key,Value value){
        root = new Node(key,value,null,null,BLACK,1);
    }
    public boolean isRED(Node root){
        return root.color;
    }
    public int size(Node node){
        if (node==null){
            return 0;
        }
        return node.size;
    }

    public Node leftRotate(Node root){
        Node h = root.right;
        root.right = h.left;
        h.left = root;
        h.color = root.color;
        root.color = RED;
        h.size = root.size;
        root.size = size(root.left)+size(root.right)+1;
        return h;
    }

    public Node rightRotate(Node root){
        Node h = root.left;
        root.left = h.right;
        h.right = root;
        h.color = root.color;
        root.color = RED;
        h.size = size(root);
        root.size = size(root.left)+size(root.right)+1;
        return h;
    }


    public void put(Key key,Value value){
        put(key,value,root);
    }
    public void inputFlipColor(Node root){
        root.color = RED;
        root.left.color = BLACK;
        root.right.color = BLACK;
    }

    public Node put(Key key,Value value,Node root){
        //新加入的结点一定是红点
        if (root==null){
            return new Node(key,value,null,null,RED,1);
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            root.right = put(key,value,root.right);
        }else if (cmp<0){
            root.left = put(key,value,root.left);
        }else {
            root.value = value;
        }
        if (!isRED(root.left)&&isRED(root.right)){
            leftRotate(root);
        }
        if (isRED(root.left)&&isRED(root.left.left)){
            rightRotate(root);
        }
        if (isRED(root.left)&&isRED(root.right)){
            inputFlipColor(root);
        }
        root.size = size(root.left)+size(root.right)+1;
        return root;
    }
    public Value get(Key key){
        Node node = get(key,root);

        return node==null?null:node.value;
    }
    public Node get(Key key, Node root){
        if (root ==null){
            return null;
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            return get(key,root.right);
        }else if (cmp<0){
            return get(key,root.left);
        }else{
            return root;
        }
    }
    public Key select(int k){
        Node node = select(k,root);
        return node==null?null:node.key;
    }
    public Node select(int k,Node root){
        if (root==null){
            return null;
        }
        int size = size(root.left);
        if (size>k){
            return select(k-size-1,root.right);
        }else if (size<k){
            return select(k,root.left);
        }else {
            return root;
        }
    }
    public int rank(Key key){
        return rank(key,root);
    }
    private int rank(Key key,Node root){
        if (root==null){
            return 0;
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            return rank(key,root.right)+1+size(root.left);
        }else if (cmp<0){
            return rank(key,root.left);
        }else {
            return size(root.left)+1;
        }

    }
    //从右边红色节点借过来一个
    private Node moveRedLeft(Node node){
            delFlipColor(node);
            if (isRED(node.right.left)){
                node.right = rightRotate(node.right);
                node = leftRotate(node);
            }
            return node;
    }
    private void delFlipColor(Node node){
        node.color = BLACK;
        node.left.color = RED;
        node.right.color = RED;
    }
    private Node balance(Node node){
        if (node == null){
            return null;
        }
        //先判断右链接
        if (isRED(node.right)){
            leftRotate(node);
        }

        if (isRED(node.left)&&isRED(node.left.left)){
            leftRotate(node);
        }
        if (isRED(node.right)&&!isRED(node.left)){
            rightRotate(node);
        }
        if (isRED(node.left)&&isRED(node.right)){
            inputFlipColor(node);
        }

        node.size = size(node.left)+size(node.right)+1;
        return node;

    }
    public boolean isEmpty(){
        return root==null;
    }

    public void delMin(){
        if(!isRED(root.left)&&!isRED(root.right)){
            root.color = RED;
        }
        root = delMin(root);
        if (!isEmpty()){
            root.color = BLACK;
        }
    }
    public Node delMin(Node root){
        if(root.left==null){
            return null;
        }
        if (!isRED(root.left)&&!isRED(root.left.left)){
            root = moveRedLeft(root);
        }
        root.left = delMin(root.left);
        return balance(root);
    }

    public void deleteMax(){
        if (!isRED(root.left)&&!isRED(root.right)){
            root.color = RED;
        }
        root = deleteMax(root);
        if (!isEmpty()){
            root.color = BLACK;
        }

    }
    private Node deleteMax(Node root){

    }
    public void delete(Key key){

    }
    public Node delete(Key key,Node root){

    }

    private class Node{
        boolean color;
        Key key;
        Value value;
        Node left;
        Node right;
        int size;

        public Node(Key key,Value value,Node left,Node right,boolean color,int size){
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
            this.color = color;
            this.size = size;
        }
    }
}
