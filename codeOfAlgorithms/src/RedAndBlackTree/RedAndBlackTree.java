package RedAndBlackTree;


import java.io.StringReader;

/**
 * @author Yuyuan Huang
 * @create 2021-06-21 22:04
 */
public class RedAndBlackTree<Key extends Comparable<Key>, Value> {
    private static final boolean RED = true;
    private static final boolean BLACK = false;


    private Node root;
    public RedAndBlackTree(Key key,Value value){
        root = new Node(key,value,null,null,BLACK,1);
    }
    public void put(Key key,Value value){
        Node node = put(key,value,root);
        node.color = BLACK;
    }
    private Node put(Key key,Value value,Node node){
        if (node==null){
            return new Node(key,value,null,null,RED,1);
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            root.right = put(key,value,root.right);
        }else if (cmp<0){
            root.left = put(key,value,root.left);
        }else {
            node.value = value;
        }
        //注意这三个条件
        if (isRed(root.right)&&isRed(root.left)){
            flapColor(root);
        }else if (isRed(root.right)&&!isRed(root.left)){
            leftRotate(root);
        }else if (isRed(root.left)&&isRed(root.left.left)){
            rightRotate(root);
        }
        root.N = size(root.left)+ size(root.right)+1;
        return root;
    }

    private class Node{
        Key key;
        Value value;
        Node left;
        Node right;
        boolean color;
        int N;

        public Node(Key key,Value value,Node left,Node right,boolean color,int N){
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
            this.color = color;
            this.N = N;
        }

    }
    public boolean isRed(Node node){
        if (node == null){
            return false;
        }
        return node.color==RED;
    }
    public int size(Node node){
        if (node==null){
            return -1;
        }
        return node.N;
    }
    public Node leftRotate(Node h){
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        x.N = h.N;
        h.N = 1+size(h.left)+size(h.right);
        return x;
    }
    public Node rightRotate(Node node){
        Node ans = node.left;
        node.left = ans.right;
        ans.right = node;
        ans.N = node.N;
        ans.color = node.color;
        node.color = RED;
        node.N = size(node.left)+size(node.right)+1;
        return ans;
    }
    public void inputFlipColor(Node node){
        if (node==null){
            return;
        }
        node.color = RED;
        node.left.color = BLACK;
        node.right.color = BLACK;
    }
    public void deleteFlipColor(Node node){
        if (node == null){
            return;
        }
        node.color = BLACK;
        node.left.color = RED;
        node.right.color = RED;
    }
    public Value get(Key key){
        return get(key,root).value;
    }
    private Node get(Key key,Node root){
        if (root==null){
            return null;
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            return get(key,root.right);
        }else if (cmp<0){
            return get(key,root.left);
        }else {
            return root;
        }
    }
    public Key select(int k){
        return select(k,root).key;
    }
    private Node select(int k,Node root){
        if (root==null){
            return null;
        }
        int t = size(root.left);
        if (t>k){
            return select(k,root.left);
        }else if (t<k){
            return select(k-t-1,root.right);
        }else {
            return root.left;
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
    public boolean isEmpty(){
        if (root==null||root.N==0){
            return true;
        }else {
            return false;
        }
    }
    private Node moveRedLeft(Node node){
        //假设节点为红色，node.left和node.left.left都为黑色
        //将node.left或者node.left的子结点之一变红
        //这里将根节点变为黑色，左右子节点变为红色
        deleteFlipColor(node);
        //如果右节点的左节点为红，那就需要先右旋后左旋
        if (isRed(node.right.left)){
            node.right = rightRotate(node.right);
            node = leftRotate(node);
        }
        //如果右节点的左节点不为红，直接返回根节点
        return node;
    }
    private Node balance(Node root){
        //如果右链接是红色的，那么进行左旋调整
        if (isRed(root.right)){
            root = leftRotate(root);
        }
        //如果左黑右红，左旋后右旋
        if(!isRed(root.left)&&isRed(root.right)){
            root = leftRotate(root);
        }
        //如果出现两个连续的红链接，右旋后变色
        if (isRed(root.left)&&isRed(root.left.left)){
            root = rightRotate(root);
        }
        //如果出现左右都为红，直接变色
        if (isRed(root.left)&&isRed(root.left)){
            deleteFlipColor(root);
        }

        root.N = size(root.left)+size(root.right)+1;
        return root;
    }
    public void deleteMin(){
        //如果根节点左侧结点和右侧节点都是黑色，那么就设置根节点为红色
        if (!isRed(root.left)&&!isRed(root.right)){
            root.color = RED;
        }
        root = deleteMin(root);
        //最后设置根节点为黑色
        if (!isEmpty()){
            root.color = BLACK;
        }

    }
    private Node deleteMin(Node root){
        if (root.left==null){
            return null;
        }
        //这么做的原因是为了维护便利的路径中不出现单个2-结点
        //如果左节点为黑色且左节点的子节点为黑色，
        if (!isRed(root.left)&&!isRed(root.left.left)){
            root = moveRedLeft(root);
        }
        //进入左节点，继续遍历
        root.left = deleteMin(root.left);
        //balance本质上是
        return balance(root);
    }

}
