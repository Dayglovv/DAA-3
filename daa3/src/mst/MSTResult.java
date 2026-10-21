package mst;
import graph.Edge;
import java.util.List;
public class MSTResult {
    public final List<Edge> edges;
    public final long totalWeight;
    public final boolean connected;
    public final long operations;

    public MSTResult(List<Edge> edges, long totalWeight, boolean connected, long operations) {
        this.edges = edges;
        this.totalWeight = totalWeight;
        this.connected = connected;
        this.operations = operations;
    }
}
