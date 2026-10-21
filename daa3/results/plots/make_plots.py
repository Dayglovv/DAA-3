"""
Makes the two plots for the report from results/tables/results.csv
Run from the project root:  python3 results/plots/make_plots.py
"""
import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

rows = []
with open("results/tables/results.csv") as f:
    for r in csv.DictReader(f):
        rows.append(r)

# group rows: one line per (algorithm, graph type)
series = {}
for r in rows:
    key = r["algorithm"] + " [" + r["graph_type"] + "]"
    series.setdefault(key, []).append((int(r["V"]), int(r["E"]), float(r["avg_ns"])))

markers = ["o", "s", "^", "D", "v", "P", "X"]


def make_plot(x_index, xlabel, title, filename):
    plt.figure(figsize=(9, 6))
    for i, (name, points) in enumerate(series.items()):
        points.sort()
        xs = [p[x_index] for p in points]
        ys = [p[2] for p in points]
        plt.plot(xs, ys, marker=markers[i % len(markers)], label=name)
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel(xlabel)
    plt.ylabel("Average execution time (ns, log scale)")
    plt.title(title)
    plt.grid(True, which="both", linestyle=":", alpha=0.6)
    plt.legend(fontsize=8)
    plt.tight_layout()
    plt.savefig(filename, dpi=150)
    plt.close()


make_plot(0, "Number of vertices V (log scale)", "Execution time vs. number of vertices",
          "results/plots/time_vs_vertices.png")
make_plot(1, "Number of edges E (log scale)", "Execution time vs. number of edges",
          "results/plots/time_vs_edges.png")
print("plots saved")
