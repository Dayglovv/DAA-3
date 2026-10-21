import connectivity.SCC;
import connectivity.TopologicalSort;
import experiments.Experiments;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import java.io.IOException;
import java.util.List;
public class Main {
    public static void main(String[] args) throws IOException {
        String mode = args.length > 0 ? args[0] : "all";
        if (mode.equals("demo") || mode.equals("all")) {
            demo();
        }
        if (mode.equals("experiments") || mode.equals("all")) {
            System.out.println("\n=== Experiments (seed fixed, 5 runs each) ===");
            Experiments.run("results/tables");
            System.out.println("Tables saved to results/tables/");
        }
    }

    static void demo() {
        String[] names = {"Center", "Airport", "Station", "University", "Port", "Park"};
        Graph roads = new Graph(names.length, false);
        roads.addEdge(0, 1, 7);
        roads.addEdge(0, 2, 3);
        roads.addEdge(0, 3, 5);
        roads.addEdge(1, 2, 6);
        roads.addEdge(2, 3, 4);
        roads.addEdge(2, 4, 8);
        roads.addEdge(3, 5, 2);
        roads.addEdge(4, 5, 9);
        roads.addEdge(1, 4, 10);

        System.out.println("=== 1. Cheapest road network (MST) ===");
        MSTResult kruskal = Kruskal.run(roads);
        MSTResult prim = Prim.run(roads);
        System.out.println("Kruskal total cost: " + kruskal.totalWeight);
        printEdges(kruskal.edges, names);
        System.out.println("Prim total cost:    " + prim.totalWeight);
        printEdges(prim.edges, names);

        System.out.println("\n=== 2. Shortest routes from " + names[0] + " (Dijkstra) ===");
        Dijkstra.Result routes = Dijkstra.run(roads, 0);
        for (int v = 1; v < names.length; v++) {
            System.out.print(names[v] + ": " + routes.dist[v] + "  path: ");
            List<Integer> path = routes.pathTo(v);
            for (int i = 0; i < path.size(); i++) {
                System.out.print(names[path.get(i)] + (i + 1 < path.size() ? " -> " : ""));
            }
            System.out.println();
        }

        Graph build = new Graph(names.length, true);
        build.addEdge(0, 2, 1); // Center -> Station
        build.addEdge(0, 3, 1); // Center -> University
        build.addEdge(2, 4, 1); // Station -> Port
        build.addEdge(3, 5, 1); // University -> Park
        build.addEdge(2, 1, 1); // Station -> Airport

        System.out.println("\n=== 3. Order of construction (topological sort) ===");
        TopologicalSort.Result topo = TopologicalSort.run(build);
        if (topo.hasCycle) {
            System.out.println("There is a cycle, no order exists");
        } else {
            for (int v : topo.order) {
                System.out.print(names[v] + " ");
            }
            System.out.println();
        }

        build.addEdge(4, 0, 1); // Port -> Center
        System.out.println("After adding Port -> Center: has cycle = " + TopologicalSort.run(build).hasCycle);

        System.out.println("\n=== 4. Strongly connected parts of the one-way street network (SCC) ===");
        SCC.Result scc = SCC.run(build);
        for (List<Integer> comp : scc.components) {
            System.out.print("{ ");
            for (int v : comp) {
                System.out.print(names[v] + " ");
            }
            System.out.println("}");
        }
    }

    private static void printEdges(List<Edge> edges, String[] names) {
        for (Edge e : edges) {
            System.out.println("   " + names[e.from] + " - " + names[e.to] + " (" + e.weight + ")");
        }
    }
}
