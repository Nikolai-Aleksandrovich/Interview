package graph;

import java.util.Stack;

/**
 * @author Yuyuan Huang
 * @create 2021-06-30 10:51
 */
public class DepthFirstPaths {
    //这个顶点是否走过？
    private boolean[] marked;
    //从起点到一个顶点的已知路径的最后一个顶点
    private int[] edgeTo;
    //起点
    private final int s;
    public DepthFirstPaths(Graph G,int s){
        marked = new boolean[G.V()];
        edgeTo = new int[G.V()];
        this.s = s;
        dfs(G,s);
    }
    private void dfs(Graph G,int v){
        marked[v] = true;
        for (int w:G.adj(v)){
            if (!marked[w]){
                edgeTo[w] = v;
                dfs(G,w);
            }
        }

    }
    public boolean hasPathTo(int v){
        return marked[v];
    }
    //检查如果从该点起没有v，则直接返回空，建立一个栈，每次在栈中放置
    public Iterable<Integer> pathTo(int v){
        if (!hasPathTo(v)){
            return null;
        }
        Stack<Integer> stack = new Stack<>();
        for (int i = v; i != s; i = edgeTo[v]) {
            stack.push(i);
        }
        stack.push(s);
        return stack;
    }
}
