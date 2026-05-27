"""
Fase 4 benchmark: corre el simulador con cada combinacion de scheduler x perfil,
parsea las metricas de "Performance Indicators" del stdout y exporta a CSV.

Requisitos previos:
- javac y java disponibles en el PATH (probado con Java 17).
- Las fuentes deben estar compiladas en BUILD_DIR (por defecto build/classes).
  Si BUILD_DIR no existe el script lo compila primero.

Uso:
    python ml_training/scripts/benchmark.py
    python ml_training/scripts/benchmark.py --runs 3

Salida: ml_training/datasets/benchmark_results.csv
"""

import argparse
import csv
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BUILD_DIR = ROOT / "build" / "classes"
OUTPUT_CSV = ROOT / "ml_training" / "datasets" / "benchmark_results.csv"
SRC_DIR = ROOT / "src"

# (scheduler, evaluator) tuples. Evaluator is only relevant for ML; ignored otherwise.
SCHEDULERS = [
    ("FCFS",   None),
    ("RR",     None),
    ("SJF_NP", None),
    ("SJF_P",  None),
    ("ML",     "RULE"),
    ("ML",     "TREE"),
]
PROFILES = {1: "Office", 2: "Development", 3: "Multimedia"}

METRIC_PATTERNS = {
    "TotalCycles":         re.compile(r"Total execution cycles:\s*(\S+)"),
    "CPUUtilization":      re.compile(r"CPU Utilization:\s*(\S+)"),
    "Throughput":          re.compile(r"Throughput:\s*(\S+)"),
    "AvgTurnaround":       re.compile(r"Average Turnaround Time:\s*(\S+)"),
    "AvgWaiting":          re.compile(r"Average Waiting Time:\s*(\S+)"),
    "AvgContextSwitches":  re.compile(r"Average Context Switches:\s*(\S+)"),
    "AvgResponse":         re.compile(r"Average Response Time:\s*(\S+)"),
}


def compile_sources() -> None:
    print(f"[build] compiling to {BUILD_DIR} ...")
    BUILD_DIR.mkdir(parents=True, exist_ok=True)
    sources = list(SRC_DIR.rglob("*.java"))
    cmd = ["javac", "-d", str(BUILD_DIR), *map(str, sources)]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print(res.stderr, file=sys.stderr)
        sys.exit(f"javac failed (exit {res.returncode})")


def run_one(scheduler: str, evaluator: str | None, profile_id: int, max_cycles: int) -> dict:
    cmd = [
        "java",
        f"-Dur_os.scheduler={scheduler}",
        f"-Dur_os.max_cycles={max_cycles}",
    ]
    if evaluator is not None:
        cmd.append(f"-Dur_os.evaluator={evaluator}")
    cmd += ["-cp", str(BUILD_DIR), "ur_os.UR_OS", str(profile_id)]
    res = subprocess.run(cmd, capture_output=True, text=True, cwd=ROOT)
    label = f"{scheduler}+{evaluator}" if evaluator else scheduler
    if res.returncode != 0:
        raise RuntimeError(f"sim crashed: {label}/{profile_id}\n{res.stderr[:500]}")

    metrics = {}
    for name, pattern in METRIC_PATTERNS.items():
        m = pattern.search(res.stdout)
        if not m:
            raise RuntimeError(f"missing metric {name} for {label}/{profile_id}")
        metrics[name] = m.group(1)
    return metrics


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--runs", type=int, default=1,
                    help="repetitions per (scheduler, profile) pair (random seed varies per JVM)")
    ap.add_argument("--max-cycles", type=int, default=10000,
                    help="MAX_SIM_CYCLES passed via -Dur_os.max_cycles (default 10000 to ensure termination)")
    ap.add_argument("--no-compile", action="store_true",
                    help="skip the javac step (assumes BUILD_DIR is current)")
    args = ap.parse_args()

    if not args.no_compile or not BUILD_DIR.exists():
        compile_sources()

    OUTPUT_CSV.parent.mkdir(parents=True, exist_ok=True)
    fields = ["Run", "Scheduler", "Evaluator", "Profile", *METRIC_PATTERNS.keys()]

    with OUTPUT_CSV.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=fields)
        writer.writeheader()

        for run in range(1, args.runs + 1):
            for sched, evaluator in SCHEDULERS:
                label = f"{sched}+{evaluator}" if evaluator else sched
                for pid, pname in PROFILES.items():
                    print(f"[run {run}] {label:10s} / {pname:11s} ... ", end="", flush=True)
                    try:
                        metrics = run_one(sched, evaluator, pid, args.max_cycles)
                        row = {
                            "Run": run,
                            "Scheduler": sched,
                            "Evaluator": evaluator or "",
                            "Profile": pname,
                            **metrics,
                        }
                        writer.writerow(row)
                        fh.flush()
                        print(f"OK  Wait={metrics['AvgWaiting']:>7s}  TAT={metrics['AvgTurnaround']:>7s}")
                    except Exception as exc:
                        print(f"FAIL ({exc})")

    print(f"\n[done] wrote {OUTPUT_CSV}")


if __name__ == "__main__":
    main()
