package graph;
import java.util.ArrayList;
import java.util.List;
public class Graph {
    private final int vertexCount;
    private final boolean directed;
    private final List<List<Edge>> adj;
    private final List<Edge> edges;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Number of vertices can't be negative");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        this.adj = new ArrayList<>();
        this.edges = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int weight) {
        checkVertex(u);
        checkVertex(v);
        edges.add(new Edge(u, v, weight));
        adj.get(u).add(new Edge(u, v, weight));
        if (!directed) {
            adj.get(v).add(new Edge(v, u, weight));
        }
    }

    public Graph transpose() {
        Graph t = new Graph(vertexCount, directed);
        for (Edge e : edges) {
            t.addEdge(e.to, e.from, e.weight);
        }
        return t;
    }

    public List<Edge> getNeighbors(int v) {
        checkVertex(v);
        return adj.get(v);
    }

    public List<Edge> getEdges() {
        return edges;
    }
    public int getVertexCount() {
        return vertexCount;
    }
    public int getEdgeCount() {
        return edges.size();
    }
    public boolean isDirected() {
        return directed;
    }
    private void checkVertex(int v) {
        if (v < 0 || v >= vertexCount) {
            throw new IllegalArgumentException("Vertex " + v + " is out of range 0.." + (vertexCount - 1));
        }
    }
}
