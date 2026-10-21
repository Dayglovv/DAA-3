package experiments;
import connectivity.SCC;
import connectivity.TopologicalSort;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
public class Experiments {
    private static final long SEED = 42;
    private static final int MAX_WEIGHT = 100;
    private static final int RUNS = 5;
    private static final int WARMUP_RUNS = 500; // not measured, only to let the JIT compiler warm up

    private static final String[] SIZE_NAMES = {"Small", "Medium", "Large"};
    private static final int[] VERTICES = {10, 100, 1000};
    private static final int[] EDGES = {30, 500, 5000};

    private interface Task {
        long run();
    }

    private static class Row {
        String algorithm;
        String graphType;
        String size;
        int v;
        int e;
        long[] times = new long[RUNS];
        double avg;
        long operations;
    }

    private static long sink = 0; // result of algorithms is saved here, so JIT can't remove the calls

    public static void run(String outputDir) throws IOException {
        collect();
        List<Row> rows = collect();

        writeResults(rows, outputDir);
        printTable(rows);
        System.out.println("(sink = " + sink + ")");
    }

    private static List<Row> collect() {
        List<Row> rows = new ArrayList<>();

        for (int i = 0; i < SIZE_NAMES.length; i++) {
            final int v = VERTICES[i];
            final int e = EDGES[i];
            String size = SIZE_NAMES[i];

            final Graph undirected = GraphGenerator.connectedUndirected(v, e, MAX_WEIGHT, SEED);
            final Graph dag = GraphGenerator.directedDag(v, e, MAX_WEIGHT, SEED, false);
            final Graph cyclic = GraphGenerator.directedDag(v, e, MAX_WEIGHT, SEED, true);
            final Graph random = GraphGenerator.randomDirected(v, e, MAX_WEIGHT, SEED);

            rows.add(measure("Kruskal", "undirected connected", size, v, e, () -> {
                var r = Kruskal.run(undirected);
                sink += r.totalWeight;
                return r.operations;
            }));
            rows.add(measure("Prim", "undirected connected", size, v, e, () -> {
                var r = Prim.run(undirected);
                sink += r.totalWeight;
                return r.operations;
            }));
            rows.add(measure("Dijkstra", "undirected connected", size, v, e, () -> {
                var r = Dijkstra.run(undirected, 0);
                sink += r.dist[v - 1];
                return r.operations;
            }));
            rows.add(measure("SCC (Kosaraju)", "random directed", size, v, e, () -> {
                var r = SCC.run(random);
                sink += r.components.size();
                return r.operations;
            }));
            rows.add(measure("SCC (Kosaraju)", "DAG", size, v, e, () -> {
                var r = SCC.run(dag);
                sink += r.components.size();
                return r.operations;
            }));
            rows.add(measure("TopoSort (Kahn)", "DAG", size, v, e, () -> {
                var r = TopologicalSort.run(dag);
                sink += r.order.size();
                return r.operations;
            }));
            rows.add(measure("TopoSort (Kahn)", "directed with cycle", size, v, e, () -> {
                var r = TopologicalSort.run(cyclic);
                sink += r.order.size();
                return r.operations;
            }));
        }

        return rows;
    }

    private static Row measure(String algorithm, String graphType, String size, int v, int e, Task task) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            task.run();
        }
        System.gc();
        Row row = new Row();
        row.algorithm = algorithm;
        row.graphType = graphType;
        row.size = size;
        row.v = v;
        row.e = e;

        long sum = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            long ops = task.run();
            long end = System.nanoTime();
            row.times[i] = end - start;
            row.operations = ops;
            sum += row.times[i];
        }
        row.avg = sum / (double) RUNS;
        return row;
    }

    private static void writeResults(List<Row> rows, String outputDir) throws IOException {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        try (PrintWriter out = new PrintWriter(dir.resolve("results.csv").toFile())) {
            out.println("algorithm,graph_type,size,V,E,run1_ns,run2_ns,run3_ns,run4_ns,run5_ns,avg_ns,operations");
            for (Row r : rows) {
                out.print(r.algorithm + "," + r.graphType + "," + r.size + "," + r.v + "," + r.e);
                for (long t : r.times) {
                    out.print("," + t);
                }
                out.println("," + String.format(java.util.Locale.US, "%.1f", r.avg) + "," + r.operations);
            }
        }

        try (PrintWriter out = new PrintWriter(dir.resolve("results.md").toFile())) {
            out.println("| Algorithm | Graph type | V | E | Avg time (ns) | Operations |");
            out.println("|---|---|---:|---:|---:|---:|");
            for (Row r : rows) {
                out.println("| " + r.algorithm + " | " + r.graphType + " | " + r.v + " | " + r.e + " | "
                        + String.format(java.util.Locale.US, "%,.0f", r.avg) + " | " + r.operations + " |");
            }
        }
    }

    private static void printTable(List<Row> rows) {
        System.out.printf("%-17s %-22s %6s %6s %14s %12s%n", "Algorithm", "Graph", "V", "E", "Avg time (ns)", "Operations");
        for (Row r : rows) {
            System.out.printf("%-17s %-22s %6d %6d %14.0f %12d%n", r.algorithm, r.graphType, r.v, r.e, r.avg, r.operations);
        }
    }
}
