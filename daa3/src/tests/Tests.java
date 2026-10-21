package tests;
import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
public class Tests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("===== DSU =====");
        testDsu();
        System.out.println("===== Small graph =====");
        testSmallGraph();
        System.out.println("===== Disconnected graph =====");
        testDisconnectedGraph();
        System.out.println("===== Several MSTs / equal weights =====");
        testEqualWeights();
        System.out.println("===== Topological sort =====");
        testTopologicalSort();
        System.out.println("===== SCC =====");
        testScc();
        System.out.println("===== Dijkstra =====");
        testDijkstra();
        System.out.println("===== Single vertex =====");
        testSingleVertex();
        System.out.println("===== Random graphs (cross-checks) =====");
        testRandomGraphs();

        System.out.println();
        System.out.println("Passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void testDsu() {
        DSU dsu = new DSU(5);
        check("DSU: at start 5 separate sets", dsu.getComponents() == 5);
        check("DSU: union(0,1) returns true", dsu.union(0, 1));
        check("DSU: union(1,2) returns true", dsu.union(1, 2));
        check("DSU: find(0), find(1), find(2) are the same",
                dsu.find(0) == dsu.find(1) && dsu.find(1) == dsu.find(2));
        check("DSU: 3 is not connected with 0", !dsu.connected(0, 3));
        check("DSU: union of the same set returns false", !dsu.union(0, 2));
        check("DSU: 3 sets are left", dsu.getComponents() == 3);

        DSU big = new DSU(1000);
        for (int i = 0; i + 1 < 1000; i++) {
            big.union(i, i + 1);
        }
        check("DSU: chain of 1000 elements is one set", big.getComponents() == 1 && big.connected(0, 999));
    }

    static void testSmallGraph() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 2);
        g.addEdge(1, 2, 3);
        g.addEdge(1, 3, 5);
        g.addEdge(2, 3, 7);

        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check("Kruskal: weight 10", k.totalWeight == 10);
        check("Prim: weight 10", p.totalWeight == 10);
        check("Kruskal: 3 edges", k.edges.size() == 3 && k.connected);
        check("Prim: 3 edges", p.edges.size() == 3 && p.connected);
        check("Kruskal and Prim give the same weight", k.totalWeight == p.totalWeight);
    }

    static void testDisconnectedGraph() {
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(0, 2, 3);
        g.addEdge(3, 4, 4);

        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check("Kruskal: disconnected graph is detected", !k.connected);
        check("Prim: disconnected graph is detected", !p.connected);
        check("Kruskal: only V-2 = 3 edges (forest)", k.edges.size() == 3);
        check("Kruskal and Prim forest weights are equal (7)", k.totalWeight == 7 && p.totalWeight == 7);
    }

    static void testEqualWeights() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 0, 1);
        g.addEdge(0, 2, 1);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check("Equal weights: Kruskal weight = 3", k.totalWeight == 3);
        check("Equal weights: Prim weight = 3", p.totalWeight == 3);
        check("Equal weights: both have V-1 edges", k.edges.size() == 3 && p.edges.size() == 3);

        Graph t = new Graph(3, false);
        t.addEdge(0, 1, 5);
        t.addEdge(1, 2, 5);
        t.addEdge(0, 2, 5);
        check("Triangle with equal weights: both algorithms give 10",
                Kruskal.run(t).totalWeight == 10 && Prim.run(t).totalWeight == 10);
    }


    static void testTopologicalSort() {
        Graph dag = new Graph(6, true);
        dag.addEdge(5, 2, 1);
        dag.addEdge(5, 0, 1);
        dag.addEdge(4, 0, 1);
        dag.addEdge(4, 1, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(3, 1, 1);
        TopologicalSort.Result r = TopologicalSort.run(dag);
        check("Topo: DAG has no cycle", !r.hasCycle);
        check("Topo: order contains all 6 vertices", r.order.size() == 6);
        check("Topo: order is correct for every edge", isValidTopoOrder(dag, r.order));

        Graph cyc = new Graph(4, true);
        cyc.addEdge(0, 1, 1);
        cyc.addEdge(1, 2, 1);
        cyc.addEdge(2, 0, 1);
        cyc.addEdge(2, 3, 1);
        TopologicalSort.Result rc = TopologicalSort.run(cyc);
        check("Topo: cycle 0->1->2->0 is detected", rc.hasCycle);

        Graph loop = new Graph(2, true);
        loop.addEdge(0, 0, 1);
        check("Topo: self-loop is a cycle", TopologicalSort.run(loop).hasCycle);

        Graph genDag = GraphGenerator.directedDag(100, 500, 20, 1, false);
        Graph genCyc = GraphGenerator.directedDag(100, 500, 20, 1, true);
        TopologicalSort.Result a = TopologicalSort.run(genDag);
        check("Topo: generated DAG (100, 500) is sorted correctly", !a.hasCycle && isValidTopoOrder(genDag, a.order));
        check("Topo: generated graph with back edge has a cycle", TopologicalSort.run(genCyc).hasCycle);
    }


    static void testScc() {
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 3, 1);
        g.addEdge(4, 5, 1);
        SCC.Result r = SCC.run(g);
        check("SCC: 3 components found", r.components.size() == 3);
        check("SCC: 0,1,2 are together", r.componentId[0] == r.componentId[1] && r.componentId[1] == r.componentId[2]);
        check("SCC: 3,4 are together", r.componentId[3] == r.componentId[4]);
        check("SCC: 5 is alone, groups are different",
                r.componentId[5] != r.componentId[3] && r.componentId[3] != r.componentId[0]);
        check("SCC: every vertex is in exactly one component", eachVertexOnce(r, 6));

        Graph dag = GraphGenerator.directedDag(50, 200, 10, 3, false);
        SCC.Result rd = SCC.run(dag);
        check("SCC: DAG with 50 vertices gives 50 components", rd.components.size() == 50);

        Graph rnd = GraphGenerator.randomDirected(100, 500, 10, 5);
        check("SCC: random graph, every vertex is in exactly one component", eachVertexOnce(SCC.run(rnd), 100));
    }

    static void testDijkstra() {
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 2);
        g.addEdge(1, 2, 3);
        g.addEdge(1, 3, 5);
        g.addEdge(2, 3, 8); // 8 and not 7, so the shortest path 0-1-3 (=9) is the only one
        Dijkstra.Result r = Dijkstra.run(g, 0);
        check("Dijkstra: dist to 0 is 0", r.dist[0] == 0);
        check("Dijkstra: dist to 1 is 4", r.dist[1] == 4);
        check("Dijkstra: dist to 2 is 2", r.dist[2] == 2);
        check("Dijkstra: dist to 3 is 9", r.dist[3] == 9);
        check("Dijkstra: path to 3 is [0, 1, 3]", r.pathTo(3).equals(Arrays.asList(0, 1, 3)));
        check("Dijkstra: vertex 4 is unreachable", !r.isReachable(4));
        check("Dijkstra: path to unreachable vertex is empty", r.pathTo(4).isEmpty());

        Graph d = new Graph(3, true);
        d.addEdge(0, 1, 1);
        d.addEdge(2, 1, 1);
        Dijkstra.Result rd = Dijkstra.run(d, 0);
        check("Dijkstra: directed edge 2->1 doesn't make 2 reachable", !rd.isReachable(2));

        Graph neg = new Graph(2, true);
        neg.addEdge(0, 1, -5);
        boolean thrown = false;
        try {
            Dijkstra.run(neg, 0);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        check("Dijkstra: negative weight throws exception", thrown);

        Graph zero = new Graph(3, false);
        zero.addEdge(0, 1, 0);
        zero.addEdge(1, 2, 0);
        check("Dijkstra: zero weights work", Dijkstra.run(zero, 0).dist[2] == 0);
    }


    static void testSingleVertex() {
        Graph u = new Graph(1, false);
        MSTResult k = Kruskal.run(u);
        MSTResult p = Prim.run(u);
        check("1 vertex: Kruskal MST is empty with weight 0", k.edges.isEmpty() && k.totalWeight == 0 && k.connected);
        check("1 vertex: Prim MST is empty with weight 0", p.edges.isEmpty() && p.totalWeight == 0 && p.connected);

        Graph d = new Graph(1, true);
        TopologicalSort.Result t = TopologicalSort.run(d);
        check("1 vertex: topological order is [0]", !t.hasCycle && t.order.equals(Arrays.asList(0)));
        check("1 vertex: SCC has 1 component", SCC.run(d).components.size() == 1);
        Dijkstra.Result r = Dijkstra.run(u, 0);
        check("1 vertex: Dijkstra dist to itself is 0", r.dist[0] == 0 && r.pathTo(0).equals(Arrays.asList(0)));
    }

    static void testRandomGraphs() {
        boolean sameWeight = true;
        boolean allTrees = true;
        for (int seed = 0; seed < 100; seed++) {
            int v = 5 + seed % 40;
            int e = Math.min(v * (v - 1) / 2, v - 1 + 3 * v);
            Graph g = GraphGenerator.connectedUndirected(v, e, 10, seed); // small weights = lots of equal weights
            MSTResult k = Kruskal.run(g);
            MSTResult p = Prim.run(g);
            if (k.totalWeight != p.totalWeight) {
                sameWeight = false;
            }
            if (!k.connected || !p.connected || k.edges.size() != v - 1 || p.edges.size() != v - 1) {
                allTrees = false;
            }
        }
        check("Random: Kruskal weight == Prim weight on 100 connected graphs", sameWeight);
        check("Random: both algorithms return V-1 edges on connected graphs", allTrees);

        boolean sameForest = true;
        Random rnd = new Random(42);
        for (int i = 0; i < 100; i++) {
            int v = 2 + rnd.nextInt(30);
            Graph g = new Graph(v, false);
            int edges = rnd.nextInt(v);
            for (int j = 0; j < edges; j++) {
                int a = rnd.nextInt(v);
                int b = rnd.nextInt(v);
                if (a != b) {
                    g.addEdge(a, b, 1 + rnd.nextInt(5));
                }
            }
            if (Kruskal.run(g).totalWeight != Prim.run(g).totalWeight) {
                sameForest = false;
            }
        }
        check("Random: Kruskal == Prim also for (maybe) disconnected graphs", sameForest);

        boolean dijkstraOk = true;
        for (int seed = 0; seed < 50; seed++) {
            int v = 10 + seed;
            Graph g = GraphGenerator.randomDirected(v, 3 * v, 20, seed);
            long[] expected = bellmanFord(g, 0);
            long[] actual = Dijkstra.run(g, 0).dist;
            if (!Arrays.equals(expected, actual)) {
                dijkstraOk = false;
            }
        }
        check("Random: Dijkstra == Bellman-Ford on 50 directed graphs (with unreachable vertices)", dijkstraOk);

        boolean consistent = true;
        for (int seed = 0; seed < 50; seed++) {
            int v = 10 + seed;
            Graph g = GraphGenerator.randomDirected(v, v + seed, 5, seed);
            boolean cycle = TopologicalSort.run(g).hasCycle;
            boolean bigScc = false;
            for (List<Integer> c : SCC.run(g).components) {
                if (c.size() > 1) {
                    bigScc = true;
                }
            }
            if (cycle != bigScc) {
                consistent = false;
            }
        }
        check("Random: 'has cycle' from Kahn matches 'has SCC with size > 1'", consistent);
    }

    static boolean isValidTopoOrder(Graph g, List<Integer> order) {
        int[] position = new int[g.getVertexCount()];
        Arrays.fill(position, -1);
        for (int i = 0; i < order.size(); i++) {
            position[order.get(i)] = i;
        }
        for (Edge e : g.getEdges()) {
            if (position[e.from] == -1 || position[e.to] == -1 || position[e.from] >= position[e.to]) {
                return false;
            }
        }
        return true;
    }

    static boolean eachVertexOnce(SCC.Result r, int v) {
        Set<Integer> seen = new HashSet<>();
        int total = 0;
        for (List<Integer> comp : r.components) {
            for (int x : comp) {
                seen.add(x);
                total++;
            }
        }
        return seen.size() == v && total == v;
    }

    static long[] bellmanFord(Graph g, int source) {
        int v = g.getVertexCount();
        long[] dist = new long[v];
        Arrays.fill(dist, Dijkstra.INF);
        dist[source] = 0;
        for (int i = 0; i < v - 1; i++) {
            for (Edge e : g.getEdges()) {
                if (dist[e.from] != Dijkstra.INF && dist[e.from] + e.weight < dist[e.to]) {
                    dist[e.to] = dist[e.from] + e.weight;
                }
            }
        }
        return dist;
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }
}
