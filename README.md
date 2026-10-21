# Assignment 3 — Smart City Infrastructure and Route Planning

Java application that represents a city as a graph and solves three kinds of problems:
the cheapest way to connect districts (MST), analysis of connections between districts
(topological sort, SCC) and shortest routes (Dijkstra).

## 1. Overview

The city is a graph:

- **Vertex** = a district of the city (numbered `0 .. V-1`)
- **Edge** = a road between two districts
- **Weight** = cost of building the road or its length (an integer from 1 to 100 in the experiments)

Two kinds of graphs are used:

| Graph | Used for |
|---|---|
| Undirected weighted | MST (Kruskal, Prim) and Dijkstra |
| Directed | Topological sort, SCC (and Dijkstra also works on it) |

What the program can do:

1. Find the **minimum spanning tree** (cheapest road network that connects all districts) with Kruskal and Prim.
2. Check if the dependencies between districts have a **cycle** and find a valid order (topological sort).
3. Find **strongly connected components** of a directed graph (SCC).
4. Find **shortest paths** from one district to all others (Dijkstra).

### Project structure

```
assignment-3/
├── src/
│   ├── Main.java                 demo + runs the experiments
│   ├── graph/                    Edge, Graph, GraphGenerator
│   ├── dsu/                      DSU
│   ├── mst/                      Kruskal, Prim, MSTResult
│   ├── connectivity/             TopologicalSort, SCC
│   ├── shortestpath/             Dijkstra
│   ├── experiments/              Experiments (time measurements)
│   └── tests/                    Tests (all checks)
├── results/
│   ├── tables/                   results.csv, results.md, test_output.txt
│   └── plots/                    2 plots + make_plots.py
└── README.md
```

### How to run

```
# compile (from the project root)
javac -d out $(find src -name "*.java")

java -cp out Main            # demo + experiments (saves tables to results/tables)
java -cp out tests.Tests     # all tests
python3 results/plots/make_plots.py   # plots (needs matplotlib)
```

In IntelliJ IDEA the same can be done by marking `src` as *Sources Root* and running `Main` / `Tests`.
No external libraries are used.

## 2. Implementation

**`Edge`** — stores `from`, `to`, `weight`. It implements `Comparable` (by weight) so it can be put into a `PriorityQueue`.

**`Graph`** — adjacency lists (`List<List<Edge>>`) + a separate list of all edges. The constructor has a flag `directed`.
For an undirected graph `addEdge(u, v, w)` writes the edge to the lists of both `u` and `v`; for a directed graph
only to the list of `u`. The list of all edges contains each edge once, it is needed for Kruskal. There is also
`transpose()` for Kosaraju.

**`GraphGenerator`** — creates graphs with a fixed seed (`java.util.Random(seed)`), so every run gives the same graphs:

- `connectedUndirected` — first a random spanning tree (every new vertex is connected to a random previous one),
  so the graph is always connected; then random extra edges without loops and duplicates until there are exactly `E` edges.
- `directedDag` — random order of vertices, edges go only from earlier to later vertex, so there are **no cycles**.
  The first `V-1` edges form a path through the whole order. With `withCycle = true` one more edge from the last
  vertex to the first one is added, so a cycle exists for sure.
- `randomDirected` — random directed edges, no restriction, so cycles and big SCCs appear.

**`DSU`** — arrays `parent` and `rank`.
`find` has *path compression* (second pass makes all vertices of the path point directly to the root),
`union` uses *union by rank* (the lower tree is attached under the higher one) and returns `false` if the
vertices were already in one set.

**`Kruskal`** — sorts a copy of the edge list by weight, goes through it with a DSU, adds an edge if `union` succeeded
and stops when `V-1` edges are chosen. If fewer than `V-1` edges were chosen, `connected = false`.

**`Prim`** — `PriorityQueue<Edge>`. Starts from vertex 0, takes the cheapest edge, skips it if the end is already in
the tree, otherwise adds the vertex and its edges. If the queue gets empty but there are still unvisited vertices
(disconnected graph), a new tree is started from the next unvisited vertex. So for a disconnected graph both Kruskal and Prim
return a minimum spanning *forest* with the same weight and `connected = false`.

**`TopologicalSort`** — Kahn's algorithm: in-degrees, queue of vertices with in-degree 0. If the number of processed
vertices is less than `V`, there is a cycle (`hasCycle = true`, the order is empty).

**`SCC`** — Kosaraju: DFS to get finishing order, transpose graph, DFS in reverse finishing order. Each DFS tree of the
second pass is one component. The result has the list of components and `componentId[v]`.

**`Dijkstra`** — `PriorityQueue<long[]>` with `{vertex, distance}`. I don't remove old entries from the queue; instead, when
an entry is taken out and its distance is bigger than the current `dist`, it is skipped. `prev[]` is used to restore the path
(`pathTo`). Unreachable vertices have distance `INF` (`Long.MAX_VALUE`) and `pathTo` returns an empty list for them.
Negative weights are not allowed — the algorithm throws `IllegalArgumentException`.

### What "operations" means in the tables

| Algorithm | Counted operations |
|---|---|
| Kruskal | comparisons during sorting + `find()` calls of DSU |
| Prim | `add()` + `poll()` of the priority queue |
| Dijkstra | `poll()` + checked edges (relaxations) |
| SCC | visited vertices + checked edges, in both DFS passes |
| Topological sort | in-degree counting + queue operations + checked edges |

The counters are plain `long` fields, so they don't change the complexity.
Note that the operations of different algorithms are *not* equally expensive (e.g. one heap operation costs more than one comparison
while sorting), so the counts are good for comparing the growth of one algorithm, but not for exact comparison between algorithms.

## 3. Complexity Analysis

`V` = number of vertices, `E` = number of edges. Graphs use adjacency lists and priority queues are binary heaps.

| Algorithm | Time | Space |
|---|---|---|
| DSU `find` / `union` | amortized O(α(V)) per operation (α = inverse Ackermann function, < 5 in practice) | O(V) |
| Kruskal | O(E log E) = O(E log V) | O(V + E) (sorted copy of edges + DSU + result) |
| Prim | O(E log V) for a heap with edges; written as O((V + E) log V) | O(V + E) (queue can hold up to E edges) |
| SCC (Kosaraju) | O(V + E) | O(V + E) (transposed graph, finishing order, recursion stack) |
| Topological sort (Kahn) | O(V + E) | O(V) extra (in-degrees, queue, result) |
| Dijkstra | O((V + E) log V) | O(V + E) (dist, prev and queue with possible old entries) |

**Why these results**

- *Kruskal:* sorting `E` edges costs O(E log E). The loop over the edges makes at most `E` DSU operations, each almost O(1),
  so sorting is the main part. Since `E ≤ V²`, `log E ≤ 2 log V`, so O(E log E) = O(E log V).
- *Prim:* every edge is added to the queue and removed from it at most once (in my implementation exactly once per edge,
  see the operation counts: `adds = polls = E`), and each heap operation costs O(log E) = O(log V).
- *SCC:* two DFS passes, each visits every vertex and edge once, plus building the transposed graph in O(V + E).
- *Topological sort:* each vertex goes into the queue once, each edge is looked at once.
- *Dijkstra:* each edge can add at most one entry into the heap, so there are at most `E` heap operations of cost O(log V).

**DSU: why path compression and union by rank?**
Without any optimization a union can create a long chain (like a linked list), and then `find` costs O(V).
*Union by rank* keeps trees low (height is at most O(log V)), because a tree gets higher only when two trees of the same
height are merged. *Path compression* flattens the tree during `find`: all visited vertices are connected directly to the root,
so the next `find` on them is almost O(1). Both together give amortized O(α(V)), which is practically a constant.
In Kruskal this means that the cycle check does not become a bottleneck and the sort stays the dominant part.

**How V and E influence performance.** Algorithms that are O(V + E) (SCC, Kahn) grow linearly with the size of the graph.
The ones with heap/sorting have an additional log factor. For a dense graph (E close to V²) the number of edges dominates,
so Kruskal's sorting and the heap in Prim/Dijkstra become more expensive; for a sparse graph (E ≈ V) the difference between
the algorithms is small.

## 4. Experimental Setup

**Graph sizes** (from the assignment):

| Size | V | E |
|---|---:|---:|
| Small | 10 | 30 |
| Medium | 100 | 500 |
| Large | 1 000 | 5 000 |

**Graphs.** All graphs are created by `GraphGenerator` with the fixed seed `42` and weights from 1 to 100:

- Kruskal, Prim, Dijkstra: connected undirected graph (random spanning tree + random extra edges). Dijkstra starts from vertex 0.
- Topological sort on a **DAG**: edges go only forward in a random vertex order → no cycles.
- Topological sort on a **graph with a cycle**: the same kind of DAG with `E-1` edges plus one back edge (last → first vertex of the order) → at least one cycle.
- SCC: on a *random directed graph* (edges are completely random, so cycles and a big SCC appear) and also on the DAG (every vertex is its own SCC).

**Measurements.**

- Time is measured with `System.nanoTime()` around one call of the algorithm; graph generation is not included.
- Every algorithm is run **5 times** on every size, each time is saved separately (`results.csv` has `run1_ns … run5_ns`), and
  `T_avg = (T1 + T2 + T3 + T4 + T5) / 5`.
- Before the measured runs there is a warm-up (the whole experiment is executed once and thrown away, then 500 not-measured calls
  before each measured group). I did this because in my first try without warm-up the Small graphs looked slower than the Large ones —
  this was caused by the JVM (interpreter / JIT compiler), not by the algorithms.
- The number of operations is explained in the table in section 2.

## 5. Results

Table (also in `results/tables/results.md`, raw times of all 5 runs in `results/tables/results.csv`).
The exact times depend on the computer, the numbers below are from my run:

| Algorithm | Graph type | V | E | Avg time (ns) | Operations |
|---|---|---:|---:|---:|---:|
| Kruskal | undirected connected | 10 | 30 | 5,214 | 139 |
| Prim | undirected connected | 10 | 30 | 3,168 | 60 |
| Dijkstra | undirected connected | 10 | 30 | 2,735 | 78 |
| SCC (Kosaraju) | random directed | 10 | 30 | 3,807 | 80 |
| SCC (Kosaraju) | DAG | 10 | 30 | 4,214 | 80 |
| TopoSort (Kahn) | DAG | 10 | 30 | 1,559 | 80 |
| TopoSort (Kahn) | directed with cycle | 10 | 30 | 1,128 | 30 |
| Kruskal | undirected connected | 100 | 500 | 43,676 | 4199 |
| Prim | undirected connected | 100 | 500 | 42,087 | 1000 |
| Dijkstra | undirected connected | 100 | 500 | 31,699 | 1203 |
| SCC (Kosaraju) | random directed | 100 | 500 | 27,864 | 1200 |
| SCC (Kosaraju) | DAG | 100 | 500 | 17,456 | 1200 |
| TopoSort (Kahn) | DAG | 100 | 500 | 5,745 | 1200 |
| TopoSort (Kahn) | directed with cycle | 100 | 500 | 1,873 | 500 |
| Kruskal | undirected connected | 1000 | 5000 | 523,739 | 56936 |
| Prim | undirected connected | 1000 | 5000 | 753,521 | 10000 |
| Dijkstra | undirected connected | 1000 | 5000 | 351,750 | 11933 |
| SCC (Kosaraju) | random directed | 1000 | 5000 | 214,763 | 12000 |
| SCC (Kosaraju) | DAG | 1000 | 5000 | 170,744 | 12000 |
| TopoSort (Kahn) | DAG | 1000 | 5000 | 57,085 | 12000 |
| TopoSort (Kahn) | directed with cycle | 1000 | 5000 | 9,243 | 5000 |

### Plots

**Execution time vs. number of vertices**

![Execution time vs. number of vertices](https://github.com/Dayglovv/DAA-3/blob/main/daa3/results/plots/time_vs_edges.png?raw=true)

**Execution time vs. number of edges**

![Execution time vs. number of edges](https://github.com/Dayglovv/DAA-3/blob/main/daa3/results/plots/time_vs_vertices.png?raw=true)

Both axes are logarithmic. In my experiment `V` and `E` grow together (E = 3V for Small, 5V for Medium and Large), so the two
plots have a very similar shape and it is not possible to see the influence of `V` and `E` separately — the plots would need graphs with
fixed `V` and changing `E` for that.

## 6. Comparison and Discussion

**Kruskal vs Prim.** Both always found the same total weight (checked in the tests on 100 random graphs, also with many equal weights).
In terms of operations Prim does much less on the Large graph: 10 000 (E adds + E polls) against 56 936 for Kruskal (mostly comparisons
of the sort, about E·log₂E ≈ 61 000 for E = 5000). In terms of time, however, Kruskal was faster on the Large graph
(about 0.52 ms vs 0.75 ms) and almost equal on Medium. The reason is probably that one operation of the heap (`PriorityQueue` of objects,
sifting up and down) is more expensive than one comparison in `List.sort` (which is highly optimized), so the number of operations
alone does not decide. I think for graphs of this size (sparse, E = 5V) there is no big practical difference; Prim is usually
recommended for dense graphs and Kruskal for sparse graphs or when the edges are already sorted. I did not test dense graphs, so I can't prove this with my own data.
Also the time of Small graphs is only a few microseconds, so it is very sensitive to noise.

**SCC on different sizes.** The time grows about linearly with V + E: the operation count is exactly `2·(V + E)` (10 → 80,
100 → 1 200, 1 000 → 12 000), which agrees with O(V + E). The random directed graph and the DAG have the same number of operations,
because Kosaraju always visits all vertices and edges in both passes; the small time differences between them are
noise / memory effects. The DFS is recursive, so for a very long path graph (hundreds of thousands of vertices) a
`StackOverflowError` would be possible; for V = 1000 there is no problem.

**Topological sort: DAG vs graph with cycle.** For the DAG the algorithm processes all vertices (`2V + 2E` operations = 12 000 for Large).
For the graph with a cycle it is much faster (9 µs vs 57 µs on Large, 5 000 operations — only the in-degree counting). This is
a special property of my generator: the back edge goes into the *first* vertex of the order, so no vertex has in-degree 0 and Kahn's algorithm
stops immediately. If the cycle was somewhere in the middle of the graph, Kahn would process the part before the cycle and then stop, so
the time would be between these two values. Anyway, the algorithm finds the cycle in O(V + E) in the worst case, and no extra algorithm is needed.

**Dijkstra.** Time grows from 2.7 µs to 32 µs to 352 µs; the operation count is about `2E` edge checks + the heap polls
(78 → 1 203 → 11 933), as expected for O((V + E) log V). On these graphs the number of polls is a bit bigger than V
(1 933 polls for 1 000 vertices) because the same vertex can be inserted into the queue several times (old entries are skipped).

**General observations.**

- Linear algorithms (Kahn, Kosaraju) are the fastest per edge; the algorithms with sorting or a heap (Kruskal, Prim, Dijkstra) are slower,
  which agrees with the extra log factor.
- Going from Medium to Large (10× vertices, 10× edges) makes the time grow roughly 8–18× (for example Kruskal 44 µs → 524 µs, Prim 42 µs → 754 µs, SCC 28 µs → 215 µs), which is about
  linear or a bit more — the log factor, and also cache/memory effects for bigger data.
- Limits of my measurements: only 5 runs, times in microseconds are noisy, and I ran everything on one computer in one JVM session.
  The *trends* are clear, but I would not trust small differences (for example Kruskal vs Prim on Medium).

## 7. Testing

All tests are in `src/tests/Tests.java`; run `java -cp out tests.Tests`. The output of my run is saved in
`results/tables/test_output.txt` — **55 checks, all passed**. JUnit is not used, a small `check(name, condition)` method prints PASS/FAIL.

| Case from the assignment | What is checked |
|---|---|
| Small graph | MST of a 4-vertex graph has weight 10 and 3 edges (Kruskal and Prim); Dijkstra distances and a path on a 5-vertex graph; Kahn on a 6-vertex DAG; SCC on a 6-vertex graph with components {0,1,2}, {3,4}, {5} |
| Disconnected graph | Kruskal and Prim report `connected = false`, give a forest with the same weight; Dijkstra marks vertices as unreachable |
| Graph with cycle | Kahn finds cycle `0→1→2→0`, a self-loop, and the generated graph with a back edge; DAG from the generator is sorted correctly |
| Several MSTs | square with a diagonal (all weights 1) and a triangle (all weights 5): weights equal for both algorithms |
| Unreachable vertices | isolated vertex gets `INF` and an empty path; in a directed graph an edge `2→1` doesn't make 2 reachable from 0 |
| Equal edge weights | see "several MSTs"; also 100 random graphs with weights 1..10 (many duplicates) |
| Single vertex | MST is empty with weight 0; topological order `[0]`; 1 SCC; Dijkstra distance to itself is 0 |

Additional checks:

- **DSU**: sets before/after union, union of the same set returns `false`, chain of 1000 elements ends up as one set.
- **Order check**: for every edge `u → v` the vertex `u` is before `v` in the result of Kahn (function `isValidTopoOrder`).
- **SCC**: every vertex is in exactly one component (also on a random graph with 100 vertices); a DAG with 50 vertices gives 50 components.
- **Cross-checks on random graphs**: Kruskal weight == Prim weight on 100 connected and 100 possibly disconnected graphs;
  Dijkstra == Bellman-Ford (simple slow algorithm) on 50 directed graphs, including unreachable vertices;
  "Kahn found a cycle" ⇔ "SCC found a component with more than one vertex" on 50 random directed graphs.
- **Negative weight** in Dijkstra throws `IllegalArgumentException`; zero weights work.

## 8. Conclusion

- **Kruskal vs Prim.** Both give a correct MST of the same weight. Kruskal is simple, uses DSU and was a bit faster
  in my tests on sparse graphs; Prim does fewer operations and is a good choice when the graph is dense or stored as adjacency lists.
  For planning the cheapest road network either one works. Both handle a disconnected graph (they give a forest and report that
  the graph is not connected), which is useful for a city where some districts are not yet connected.
- **Topological sort (Kahn)** is the best tool for the order of construction / dependencies between districts, and at the same time it
  detects cycles (an impossible order) in O(V + E).
- **SCC (Kosaraju)** shows groups of districts that can reach each other (for example parts of a one-way street network) in O(V + E),
  so it is good to check whether a street network is "fully drivable".
- **Dijkstra** is the right choice for shortest routes when all distances are non-negative; it also tells which districts are unreachable.
  With negative weights another algorithm (Bellman-Ford) would be needed.
- **DSU** with path compression and union by rank makes the connectivity check almost constant time, which makes Kruskal efficient.

What I learned: the theoretical complexity matches the measured growth well, but for small inputs the constant factors,
the JVM warm-up and the kind of data structure (heap vs sorting) matter more than the O-notation.
