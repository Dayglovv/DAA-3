package connectivity;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
public class SCC {

    public static class Result {
        public final List<List<Integer>> components;
        public final int[] componentId;
        public final long operations;

        Result(List<List<Integer>> components, int[] componentId, long operations) {
            this.components = components;
            this.componentId = componentId;
            this.operations = operations;
        }
    }

    private long operations;

    public static Result run(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC needs a directed graph");
        }
        return new SCC().compute(graph);
    }

    private Result compute(Graph graph) {
        int v = graph.getVertexCount();
        operations = 0;

        boolean[] visited = new boolean[v];
        List<Integer> finishOrder = new ArrayList<>();
        for (int i = 0; i < v; i++) {
            if (!visited[i]) {
                dfsFirst(graph, i, visited, finishOrder);
            }
        }

        Graph transposed = graph.transpose();
        int[] componentId = new int[v];
        java.util.Arrays.fill(componentId, -1);
        List<List<Integer>> components = new ArrayList<>();

        for (int i = v - 1; i >= 0; i--) {
            int start = finishOrder.get(i);
            if (componentId[start] == -1) {
                List<Integer> comp = new ArrayList<>();
                dfsSecond(transposed, start, componentId, components.size(), comp);
                components.add(comp);
            }
        }
        return new Result(components, componentId, operations);
    }

    private void dfsFirst(Graph g, int u, boolean[] visited, List<Integer> finishOrder) {
        visited[u] = true;
        operations++;
        for (Edge e : g.getNeighbors(u)) {
            operations++;
            if (!visited[e.to]) {
                dfsFirst(g, e.to, visited, finishOrder);
            }
        }
        finishOrder.add(u);
    }

    private void dfsSecond(Graph g, int u, int[] componentId, int id, List<Integer> comp) {
        componentId[u] = id;
        comp.add(u);
        operations++;
        for (Edge e : g.getNeighbors(u)) {
            operations++;
            if (componentId[e.to] == -1) {
                dfsSecond(g, e.to, componentId, id, comp);
            }
        }
    }
}
