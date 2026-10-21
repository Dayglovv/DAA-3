package shortestpath;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
public class Dijkstra {

    public static final long INF = Long.MAX_VALUE;

    public static class Result {
        public final int source;
        public final long[] dist;
        public final int[] prev;
        public final long operations;

        Result(int source, long[] dist, int[] prev, long operations) {
            this.source = source;
            this.dist = dist;
            this.prev = prev;
            this.operations = operations;
        }

        public boolean isReachable(int target) {
            return dist[target] != INF;
        }

        public List<Integer> pathTo(int target) {
            List<Integer> path = new ArrayList<>();
            if (!isReachable(target)) {
                return path;
            }
            for (int cur = target; cur != -1; cur = prev[cur]) {
                path.add(cur);
            }
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph graph, int source) {
        int v = graph.getVertexCount();
        if (source < 0 || source >= v) {
            throw new IllegalArgumentException("Source vertex " + source + " does not exist");
        }
        for (Edge e : graph.getEdges()) {
            if (e.weight < 0) {
                throw new IllegalArgumentException("Dijkstra can't work with negative weights: " + e);
            }
        }

        long[] dist = new long[v];
        int[] prev = new int[v];
        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[source] = 0;
        long operations = 0;

        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[1], b[1]));
        pq.add(new long[]{source, 0});

        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            operations++;
            int u = (int) top[0];
            if (top[1] > dist[u]) {
                continue;
            }
            for (Edge e : graph.getNeighbors(u)) {
                operations++;
                long newDistance = dist[u] + e.weight;
                if (newDistance < dist[e.to]) {
                    dist[e.to] = newDistance;
                    prev[e.to] = u;
                    pq.add(new long[]{e.to, newDistance});
                }
            }
        }
        return new Result(source, dist, prev, operations);
    }
}
