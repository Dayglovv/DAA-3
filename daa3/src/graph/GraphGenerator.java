package graph;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    public static Graph connectedUndirected(int v, int e, int maxWeight, long seed) {
        if (v < 1 || e < v - 1 || e > (long) v * (v - 1) / 2) {
            throw new IllegalArgumentException("Impossible number of edges for connected graph");
        }
        Random rnd = new Random(seed);
        Graph g = new Graph(v, false);
        Set<Long> used = new HashSet<>();

        int[] perm = shuffledVertices(v, rnd);
        int count = 0;
        for (int i = 1; i < v; i++) {
            int a = perm[i];
            int b = perm[rnd.nextInt(i)]; // connect to some vertex that is already in the tree
            used.add(undirectedKey(a, b, v));
            g.addEdge(a, b, 1 + rnd.nextInt(maxWeight));
            count++;
        }
        while (count < e) {
            int a = rnd.nextInt(v);
            int b = rnd.nextInt(v);
            if (a == b || !used.add(undirectedKey(a, b, v))) {
                continue;
            }
            g.addEdge(a, b, 1 + rnd.nextInt(maxWeight));
            count++;
        }
        return g;
    }

    public static Graph directedDag(int v, int e, int maxWeight, long seed, boolean withCycle) {
        int dagEdges = withCycle ? e - 1 : e;
        if (v < 2 || dagEdges < v - 1 || dagEdges > (long) v * (v - 1) / 2) {
            throw new IllegalArgumentException("Impossible number of edges for this directed graph");
        }
        Random rnd = new Random(seed);
        Graph g = new Graph(v, true);
        Set<Long> used = new HashSet<>();
        int[] order = shuffledVertices(v, rnd);

        int count = 0;
        for (int i = 0; i + 1 < v; i++) {
            used.add((long) i * v + (i + 1));
            g.addEdge(order[i], order[i + 1], 1 + rnd.nextInt(maxWeight));
            count++;
        }
        while (count < dagEdges) {
            int i = rnd.nextInt(v);
            int j = rnd.nextInt(v);
            if (i == j) {
                continue;
            }
            if (i > j) {
                int tmp = i;
                i = j;
                j = tmp;
            }
            if (!used.add((long) i * v + j)) {
                continue;
            }
            g.addEdge(order[i], order[j], 1 + rnd.nextInt(maxWeight));
            count++;
        }
        if (withCycle) {
            g.addEdge(order[v - 1], order[0], 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    public static Graph randomDirected(int v, int e, int maxWeight, long seed) {
        if (v < 2 || e > (long) v * (v - 1)) {
            throw new IllegalArgumentException("Impossible number of edges for this directed graph");
        }
        Random rnd = new Random(seed);
        Graph g = new Graph(v, true);
        Set<Long> used = new HashSet<>();
        int count = 0;
        while (count < e) {
            int a = rnd.nextInt(v);
            int b = rnd.nextInt(v);
            if (a == b || !used.add((long) a * v + b)) {
                continue;
            }
            g.addEdge(a, b, 1 + rnd.nextInt(maxWeight));
            count++;
        }
        return g;
    }

    private static long undirectedKey(int a, int b, int v) {
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        return (long) min * v + max;
    }

    private static int[] shuffledVertices(int v, Random rnd) {
        int[] p = new int[v];
        for (int i = 0; i < v; i++) {
            p[i] = i;
        }
        for (int i = v - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        return p;
    }
}
