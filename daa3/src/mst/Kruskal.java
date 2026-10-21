package mst;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
public class Kruskal {

    public static MSTResult run(Graph graph) {
        int v = graph.getVertexCount();
        long[] comparisons = {0};

        List<Edge> sorted = new ArrayList<>(graph.getEdges());
        sorted.sort((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        DSU dsu = new DSU(v);
        List<Edge> result = new ArrayList<>();
        long total = 0;

        for (Edge e : sorted) {
            if (result.size() == v - 1) {
                break; // tree is ready, no need to look further
            }
            if (dsu.union(e.from, e.to)) {
                result.add(e);
                total += e.weight;
            }
        }
        boolean connected = (v == 0) || result.size() == v - 1;
        return new MSTResult(result, total, connected, comparisons[0] + dsu.getOperations());
    }
}
