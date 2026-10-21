package mst;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
public class Prim {

    public static MSTResult run(Graph graph) {
        int v = graph.getVertexCount();
        boolean[] inTree = new boolean[v];
        List<Edge> result = new ArrayList<>();
        long total = 0;
        long operations = 0;

        PriorityQueue<Edge> pq = new PriorityQueue<>();

        for (int start = 0; start < v; start++) {
            if (inTree[start]) {
                continue;
            }
            inTree[start] = true;
            for (Edge e : graph.getNeighbors(start)) {
                pq.add(e);
                operations++;
            }

            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                operations++;
                if (inTree[e.to]) {
                    continue;
                }
                inTree[e.to] = true;
                result.add(e);
                total += e.weight;
                for (Edge next : graph.getNeighbors(e.to)) {
                    if (!inTree[next.to]) {
                        pq.add(next);
                        operations++;
                    }
                }
            }
        }

        boolean connected = (v == 0) || result.size() == v - 1;
        return new MSTResult(result, total, connected, operations);
    }
}
