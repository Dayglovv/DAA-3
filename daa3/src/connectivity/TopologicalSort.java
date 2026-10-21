package connectivity;
import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
public class TopologicalSort {

    public static class Result {
        public final List<Integer> order;
        public final boolean hasCycle;
        public final long operations;

        Result(List<Integer> order, boolean hasCycle, long operations) {
            this.order = order;
            this.hasCycle = hasCycle;
            this.operations = operations;
        }
    }

    public static Result run(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort needs a directed graph");
        }
        int v = graph.getVertexCount();
        int[] inDegree = new int[v];
        long operations = 0;

        for (Edge e : graph.getEdges()) {
            inDegree[e.to]++;
            operations++;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < v; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
                operations++;
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            operations++;
            order.add(u);
            for (Edge e : graph.getNeighbors(u)) {
                operations++;
                inDegree[e.to]--;
                if (inDegree[e.to] == 0) {
                    queue.add(e.to);
                    operations++;
                }
            }
        }

        boolean hasCycle = order.size() != v;
        if (hasCycle) {
            order = new ArrayList<>();
        }
        return new Result(order, hasCycle, operations);
    }
}
