package RedAndBlackTree;

import org.w3c.dom.Node;

import javax.management.ValueExp;

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
    public Key min(){
        return min(root).key;
    }
    public Node min(Node root){
        if (root.left == null){
            return root;
        }else {
            return min(root.left);
        }
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
        if (isRED(node.right)&&!isRED(node.left)){
            leftRotate(node);
        }
        if (isRED(node.left)&&isRED(node.left.left)){
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
    public Node moveRedRight(Node node){
        //当结点为红色，且两个根节点都是黑色
        //那么将right或者right的子节点变红
        //flipcolor将子节点变红,根节点变黑
        delFlipColor(node);
        //如果左左为红，右旋
        if(!isRED(node.left.left)){
            node = rightRotate(node);
        }
        return node;
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
        //判断left是否为红，如果是，则从left右旋，使一个结点右移
        if (isRED(root.left)){
            root = rightRotate(root);
        }
        if (root.right==null){
            return null;
        }
        if(!isRED(root.right)&&!isRED(root.right.left)){
            root = moveRedRight(root);
        }
        root.right = deleteMax(root.right);
        return balance(root);

    }
    public void delete(Key key){
        if (!isRED(root.left)&&!isRED(root.right)){
            root.color = RED;
        }
        root = delete(key,root);
        if (!isEmpty()){
            root.color = BLACK;
        }
    }
    public Node delete(Key key,Node root){
        //cmp is proposed first
        int cmp = key.compareTo(root.key);
        //there are two situation,left or middle/right
        if (cmp<0){
            //if left and leftleft are black,then move right2left
            if (!isRED(root.left)&&!isRED(root.left.left)){
                root = moveRedLeft(root);
            }
            //recursively doing so
            root.left = delete(key,root.left);
        }else{
            //如果该店左子节点为红，右移
            if (isRED(root.left)){
                root = rightRotate(root);
            }
            //如果该点就是要删除的点，而且该点的右子节点为空
            //直接返回空
            if (cmp==0&&root.right == null){
                return null;
            }
            //如果右节点为黑，右节点的左节点也为黑，那么从左借节点
            if (!isRED(root.right)&&!isRED(root.right.left)){
                root = moveRedRight(root);
            }
            if (cmp==0){
                //当找到要删除的节点是，要找到它的后继节点，然后吧后继节点替换掉它本身
                //首先获得后继节点的value，设置为root的value，找后继节点，使用get，root.right,min(root.right)作为参数
                root.value = get(min(root.right).key,root.right).value;
                //其次找到key
                root.key = min(root.right).key;
                //最后使用删除最小节点的办法，删除后继节点
                root.right = delMin(root.right);
            }else {
                //递归进行
                root.right = delete(key,root.right);
            }

        }
        return balance(root);
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
class MyRBT<Key extends Comparable<Key>,Value>{
    private Node root;
    private final static boolean RED = true;
    private static final boolean BLACK = false;

    public MyRBT(Key key,Value value){
        root = new Node(key,value,BLACK,1,null,null);
    }




    private class Node{
        boolean color;
        int size;
        Node left;
        Node right;
        Key key;
        Value value;
        public Node(Key key, Value value,boolean color,int size,Node left,Node right){
            this.key = key;
            this.value = value;
            this.color = color;
            this.left = left;
            this.right = right;
            this.size = size;
        }
    }
    public boolean isRed(Node node){
        return node.color;
    }
    public int size(Node node){
        if (node ==null){
            return 0;
        }
        return node.size;
    }
    public Node leftRotate(Node h){
        Node x = h.right;
        x.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        x.size = h.size;
        h.size = h.left.size+h.right.size+1;
        return x;
    }
    public Node rightRotate(Node h){
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        x.size = h.size;
        h.size = h.left.size+h.right.size+1;
        return x;
    }
    public void flipColor(Node node ){
        node.color = RED;
        node.left.color = BLACK;
        node.right.color = BLACK;
    }
    public Node min(){
        return min(root);
    }
    public Node min(Node node){
        if (node == null){
            return null;
        }
        if (node.left!=null){
            return min(node.left);
        }else {
            return node;
        }
    }
    public void put(Key key,Value value){
        root = put(key,value,root);
        root.color = BLACK;
    }
    public Node put(Key key,Value value,Node node){
        if (node == null){
            return new Node(key,value,RED,1,null,null);
        }
        int cmp = key.compareTo(root.key);
        if (cmp>0){
            node.right = put(key,value,node.right);
        }else if (cmp<0){
            node.left = put(key,value,node.left);
        }else {
            node.value = value;
        }
        if (!isRed(node.left)&&isRed(node.right)){
            rightRotate(node);
        }
        if (isRed(node.left)&&isRed(node.left.left)){
            leftRotate(node);
        }
        if (isRed(node.left)&&isRed(node.right)){
            flipColor(node);
        }
        node.size = node.left.size+node.right.size+1;
        return node;

    }
    public Value get(Key key){
        return get(root,key)==null? null:get(root,key).value;
    }
    public Node get(Node node,Key key){
        if (node == null){
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp>0){
            return get(node.right,key);
        }else if (cmp<0){
            return get(node.left,key);
        }else {
            return node;
        }
    }
    public Key select(int k){
        Node node = select(root,k);
        return node==null? null:node.key;
    }
    public Node select(Node node,int k){
        if (node == null){
            return null;
        }
        int size = node.left.size;
        if (size>k){
            return select(node.left,k);
        }else if (size<k){
            return select(node.right,k-size-1);
        }else {
            return node;
        }


    }
    public void delMin(){
        if (!isRed(root.left)&&!isRed(root.right)){
            root.color=RED;
        }
        root = delMin(root);
        if (root!=null){
            root.color=BLACK;
        }

    }
    public Node delMin(Node node){
        if (node.left==null){
            return null;
        }
        if (!isRed(node.left)&&!isRed(node.left.left)){
            node = moveRightLeft(node);
        }
        node.left = delMin(node.left);
        return

    }
    public Node balance(Node node){
        if (node == null){
            return null;
        }
        if (node.right.color==RED){
            node = leftRotate(node);
        }
        if (!isRed(node.left)&&isRed(node.right)){
            node = leftRotate(node);
        }
        if (isRed(node.left)&&isRed(node.left.left)){
            node = rightRotate(node);
        }
        if (isRed(node.left)&&isRed(node.right)){
            flipColor(node);
        }
    }
    public Node moveRightLeft(Node node){
        flipColor(node);
        if (node.right.left.color==RED){
            node.right = rightRotate(node.right);
            node = leftRotate(node);
        }
        return node;

    }
    public void delFlipColor(Node node){
        node.color = BLACK;
        node.left.color = node.right.color = RED;
    }






}