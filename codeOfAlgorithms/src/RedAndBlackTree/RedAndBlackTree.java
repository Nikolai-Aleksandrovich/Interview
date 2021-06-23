package RedAndBlackTree;



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
    public void flapColor(Node node){
        if (node == null){
            return;
        }
        node.color = RED;
        node.left.color = BLACK;
        node.right.color = BLACK;
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

    }

}
