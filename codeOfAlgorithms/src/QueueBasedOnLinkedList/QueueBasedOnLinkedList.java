package QueueBasedOnLinkedList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Yuyuan Huang
 * @create 2021-06-19 17:22
 */
public class QueueBasedOnLinkedList<Items> implements Iterable<Items> {
    private int N;
    private Node head;
    private Node tail;
    private class Node<Items>{
        Items items;
        Node next;
        Node prev;
        public Node(Items items,Node next,Node prev){
            this.items = items;
            this.next = next;
            this.prev = prev;
        }
    }
    public void pushFirst(Items items){
        N++;
        Node node = new Node(items,head,null);
        if (head==null){
            head = node;
            return;
        }
        head.prev = node;
        head = node;
    }
    public void pushLast(Items items){
        N++;
        Node node = new Node(items,null,tail);
        if (tail == null){
            tail = node;
        }
        tail.next = node;
        tail = node;

    }
    public Items pollFirst(){
        if (head == null){
            throw new NoSuchElementException();
        }
        Items items = (Items) head.items;

        Node node = head.next;
        head.next = head;
        head = node;
        if (head == null){
            N--;
            return items;

        }
        head.prev = null;
        N--;
        return items;
    }
    public Items poolLast(){
        if (tail==null){
            throw new NoSuchElementException();
        }
        Items items = (Items) tail.items;
        Node node = tail.prev;
        tail.next = tail;
        tail = node;
        if (tail == null){
            return items;
        }
        tail.next=null;
        return items;
    }
    public boolean isEmpty(){
        return head == null && tail == null;
    }
    @Override
    public Iterator<Items> iterator() {
        return null;
    }
}
