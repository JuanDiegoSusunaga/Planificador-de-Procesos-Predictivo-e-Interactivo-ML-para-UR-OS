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


def main():
    df = pd.read_csv(CSV_PATH)
    df["Label"] = df.apply(make_label, axis=1)

    for col in [m[0] for m in METRICS]:
        df[col] = pd.to_numeric(df[col], errors="coerce")

    written = []
    for metric, title, unit in METRICS:
        path = plot_metric(df, metric, title, unit)
        written.append(path)
        print(f"  wrote {path.relative_to(ROOT)}")

    print(f"\n[done] {len(written)} figuras en {OUT_DIR.relative_to(ROOT)}/")


if __name__ == "__main__":
    main()
