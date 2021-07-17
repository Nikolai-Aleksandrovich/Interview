package graph;

/**
 * @author Yuyuan Huang
 * @create 2021-06-30 12:02
 */
public class CC {
    //标记是否经过这个点
    private boolean[] marked;
    //将连通分量当作数组保存
    private int[] id;
    private int count;

    public CC(Graph G){
        marked = new boolean[G.V()];
        id = new int[G.V()];
        for (int s = 0;s<G.V();s++){
            if (!marked[s]){
                dfs(G,s);
                count++;
            }
        }
    }
    private  void dfs(Graph G,int v){
        marked[v] = true;
        id[v] = count;
        for (int w:G.adj(v)){
            if (!marked[w]){
                dfs(G,w);
            }
        }
    }
    public boolean connected(int v,int w){
        return id[v]==id[w];
    }
    public int id(int v){
        return id[v];
    }
    public int count(){
        return count;
    }
}
