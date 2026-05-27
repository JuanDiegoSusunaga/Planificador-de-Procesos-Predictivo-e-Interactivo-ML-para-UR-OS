"""
Genera los graficos de Fase 4 a partir de benchmark_results.csv.

Salida: ml_training/figures/<metric>_by_profile.png

Uso:
    python ml_training/scripts/plot_benchmark.py
"""

from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd

ROOT = Path(__file__).resolve().parents[2]
CSV_PATH = ROOT / "ml_training" / "datasets" / "benchmark_results.csv"
OUT_DIR = ROOT / "ml_training" / "figures"
OUT_DIR.mkdir(parents=True, exist_ok=True)

# Metricas a graficar (todas: menor es mejor, salvo CPUUtilization y Throughput).
METRICS = [
    ("AvgWaiting",         "Average Waiting Time",        "ciclos"),
    ("AvgTurnaround",      "Average Turnaround Time",     "ciclos"),
    ("AvgResponse",        "Average Response Time",       "ciclos"),
    ("CPUUtilization",     "CPU Utilization",             "fracción"),
    ("Throughput",         "Throughput",                  "procesos/ciclo"),
    ("AvgContextSwitches", "Average Context Switches",    "switches/proceso"),
]

# Orden estable de schedulers para los grupos del barchart.
SCHEDULER_ORDER = ["FCFS", "RR", "SJF_NP", "SJF_P", "ML+RULE", "ML+TREE"]
PROFILE_ORDER = ["Office", "Development", "Multimedia"]


def make_label(row) -> str:
    if pd.isna(row["Evaluator"]) or row["Evaluator"] == "":
        return row["Scheduler"]
    return f"{row['Scheduler']}+{row['Evaluator']}"


def plot_metric(df: pd.DataFrame, metric: str, title: str, unit: str) -> Path:
    fig, ax = plt.subplots(figsize=(11, 6))

    # Promediar por (Scheduler, Profile) en caso de --runs > 1.
    grouped = df.groupby(["Label", "Profile"])[metric].mean().unstack("Profile")
    grouped = grouped.reindex(index=SCHEDULER_ORDER, columns=PROFILE_ORDER)

    grouped.plot(kind="bar", ax=ax, width=0.78, edgecolor="black", linewidth=0.5)

    ax.set_title(f"{title} por scheduler y perfil")
    ax.set_xlabel("Scheduler")
    ax.set_ylabel(f"{title} ({unit})")
    ax.set_xticklabels(ax.get_xticklabels(), rotation=0)
    ax.grid(axis="y", alpha=0.3, linestyle="--")
    ax.legend(title="Perfil", loc="best")
    ax.axhline(0, color="black", linewidth=0.6)

    # Etiquetas de valor encima de cada barra.
    for container in ax.containers:
        ax.bar_label(container, fmt="%.0f" if unit == "ciclos" else "%.2f",
                     padding=2, fontsize=8, rotation=90)

    fig.tight_layout()
    out = OUT_DIR / f"{metric.lower()}_by_profile.png"
    fig.savefig(out, dpi=130)
    plt.close(fig)
    return out


def plot_response_vs_utilization(df: pd.DataFrame) -> Path:
    """Scatter Response Time vs Utilization — el grafico que pide TEAM_ROLES."""
    fig, ax = plt.subplots(figsize=(10, 7))

    profile_colors = {"Office": "#1f77b4", "Development": "#ff7f0e", "Multimedia": "#2ca02c"}
    scheduler_markers = {
        "FCFS": "o", "RR": "s", "SJF_NP": "^", "SJF_P": "D",
        "ML+RULE": "*", "ML+TREE": "P",
    }

    for _, row in df.iterrows():
        profile = row["Profile"]
        label = row["Label"]
        color = profile_colors.get(profile, "gray")
        marker = scheduler_markers.get(label, "x")
        ax.scatter(
            row["CPUUtilization"], row["AvgResponse"],
            s=180 if label.startswith("ML+") else 110,
            c=color, marker=marker, edgecolor="black", linewidth=0.7,
            alpha=0.85,
        )
        ax.annotate(label, (row["CPUUtilization"], row["AvgResponse"]),
                    xytext=(6, 4), textcoords="offset points", fontsize=8)

    # Leyenda con dos partes: perfiles (color) y schedulers (marker).
    from matplotlib.lines import Line2D
    legend_profile = [Line2D([0], [0], marker="o", linestyle="", color=c,
                             markeredgecolor="black", markersize=10, label=p)
                      for p, c in profile_colors.items()]
    legend_sched = [Line2D([0], [0], marker=m, linestyle="", color="gray",
                           markeredgecolor="black", markersize=10, label=s)
                    for s, m in scheduler_markers.items()]
    leg1 = ax.legend(handles=legend_profile, title="Perfil", loc="upper left")
    ax.add_artist(leg1)
    ax.legend(handles=legend_sched, title="Scheduler", loc="upper right", ncol=2)

    ax.set_xlabel("CPU Utilization (fracción)")
    ax.set_ylabel("Average Response Time (ciclos)")
    ax.set_title("Response Time vs CPU Utilization — trade-off por (scheduler, perfil)")
    ax.grid(True, alpha=0.3, linestyle="--")
    ax.invert_yaxis()  # menor AvgResponse arriba = "mejor arriba"

    fig.tight_layout()
    out = OUT_DIR / "response_vs_utilization.png"
    fig.savefig(out, dpi=130)
    plt.close(fig)
    return out


def main():
    df_all = pd.read_csv(CSV_PATH)
    df_all["Label"] = df_all.apply(make_label, axis=1)

    for col in [m[0] for m in METRICS]:
        df_all[col] = pd.to_numeric(df_all[col], errors="coerce")

    # Los plots por perfil/scatter usan workload=default para evitar mezclar
    # corridas con muy distinta carga. El CSV conserva las filas de stress
    # para analisis separados.
    if "Workload" in df_all.columns:
        df = df_all[df_all["Workload"] == "default"].copy()
    else:
        df = df_all

    written = []
    for metric, title, unit in METRICS:
        path = plot_metric(df, metric, title, unit)
        written.append(path)
        print(f"  wrote {path.relative_to(ROOT)}")

    scatter = plot_response_vs_utilization(df)
    written.append(scatter)
    print(f"  wrote {scatter.relative_to(ROOT)}")

    print(f"\n[done] {len(written)} figuras en {OUT_DIR.relative_to(ROOT)}/")


if __name__ == "__main__":
    main()
