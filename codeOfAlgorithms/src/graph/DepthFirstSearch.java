package graph;

/**
 * @author Yuyuan Huang
 * @create 2021-06-30 10:19
 */
public class DepthFirstSearch {
    //是否标记
    private boolean[] marked;
    //遍历过的数量
    private int count;
    //初始化标记节点数量，并启动深度优先搜索
    public DepthFirstSearch(Graph G,int s){
        marked = new boolean[G.V()];
        dfs(G,s);
    }
    //深度优先搜索，标记当前节点位true，将遍历过节点的数量＋1，对于当前结点的所有邻接结点，进行深度优先搜索
    private void dfs(Graph G,int v){
        //
        marked[v] = true;
        count++;
        for(int w:G.adj(v)){
            if (!marked[w]){
                dfs(G,w);
            }
        }
    }
    //返回w结点是否标记
    public boolean marked(int w){
        return marked[w];
    }
    //返回标记数量
    public int count(){
        return count;
    }
}
