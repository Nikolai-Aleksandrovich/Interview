package graph;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * @author Yuyuan Huang
 * @create 2021-06-30 11:31
 */
public class BreadthFirstOaths {
    //到达该顶点的最短路径已知嘛？
    private boolean[] marked;
    //到达该顶点的路径上最后一个顶点
    private int[] edgeTo;
    //起点
    private final int s;
    public BreadthFirstOaths(Graph G,int s){
        marked = new boolean[G.V()];
        edgeTo = new int[G.V()];
        this.s = s;
        bfs(G,s);
    }
    private void bfs(Graph G,int s){
        Queue<Integer> queue = new LinkedList<>();
        marked[s] = true;
        queue.offer(s);
        while (!queue.isEmpty()){
            int temp = queue.poll();
            for (int w:G.adj(temp)){
                if (!marked[w]){
                    edgeTo[w] = temp;
                    marked[w]= true;
                    queue.offer(w);

                }
            }
        }
    }
    public boolean hasPathTo(int v){
        return marked[v];
    }
    public Iterable<Integer> pathTo(int v){
        if (!hasPathTo(v)){
            return null;
        }
        Stack<Integer> stack = new Stack<>();
        for (int i = v; i !=s ; i=edgeTo[i]) {
            stack.push(i);
        }
        stack.push(s);
        return stack;
    }
}
