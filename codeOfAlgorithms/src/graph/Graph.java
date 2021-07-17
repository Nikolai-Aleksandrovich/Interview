package graph;

/**
 * @author Yuyuan Huang
 * @create 2021-06-29 22:08
 */
public class Graph {
    //顶点数目
    private final int V;
    //边的数目
    private int E;
    //邻接表
    private Bag<Integer>[] adj;
    public Graph(int V){
        this.V = V;
        this.E = 0;
        adj = (Bag<Integer>[]) new Bag[V];
        for (int i = 0; i < V; i++) {
            adj[i] = new Bag<Integer>();
        }
    }
    //读取数据并购图
    public Graph(In in){
        this(in.readInt());
        int E = in.readInt();
        for (int i = 0; i < E; i++) {
            //添加一条边
            //读取一个顶点
            int v = in.readInt();
            //读取另一个顶点
            int w = in.readInt();
            //添加边
            addEdge(v,w);
        }
    }
    //结点的数量
    public int V(){
        return V;
    }
    //边的数量
    public int E(){
        return E;
    }
    //添加一条边
    public void addEdge(int v,int w){
        adj[v].add(w);
        adj[w].add(v);
        E++;
    }
    //返回一个结点的所有邻接结点
    public Iterable<Integer> adj(int v){
        return adj(v);
    }
}

