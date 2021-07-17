package graph;

/**
 * @author Yuyuan Huang
 * @create 2021-06-30 14:46
 */
public class Cycle {
    private boolean marked[];
    private boolean hasCycle;
    public Cycle(Graph G){
        marked = new boolean[G.V()];
        for (int s = 0;s<G.V();s++){
            if (!marked[s]){
                dfs(G,s,s);
            }
        }
    }
    //深度优先搜索，接受两个顶点
    private void dfs(Graph G,int v,int u){
        //设置v点为已经经历过
        marked[v] = true;
        //对于v点的所有临界结点，进行深度优先搜索
        for(int w:G.adj(v)){
            //如果没有走过，那就对于该点和之前这点进行dfs
            if (!marked[w]){
                dfs(G,w,v);
            }else if (w!=u){
                hasCycle=true;
            }
        }

    }
    public boolean hasCycle(){
        return hasCycle;
    }
}
