import csv
import json
import re
import sys
from pathlib import Path


def read_json(path: Path):
    with path.open() as f:
        return json.load(f)


def metric(metrics, name):
    value = metrics.get(name)
    if value is None:
        return None
    return {
        "avg": value.get("avg"),
        "med": value.get("med"),
        "p90": value.get("p(90)"),
        "p95": value.get("p(95)"),
        "max": value.get("max"),
        "count": value.get("count"),
        "rate": value.get("rate"),
    }


def parse_docker_stats(path: Path):
    rows = {}
    with path.open() as f:
        reader = csv.reader(f)
        for row in reader:
            if len(row) < 4:
                continue
            _, name, cpu_raw, mem_raw = row[:4]
            cpu_match = re.match(r"([0-9.]+)%", cpu_raw.strip())
            mem_match = re.match(r"([0-9.]+)([KMG]iB) / ([0-9.]+)([KMG]iB)", mem_raw.strip())
            if not cpu_match or not mem_match:
                continue

            cpu_value = float(cpu_match.group(1))
            mem_value = float(mem_match.group(1))
            mem_unit = mem_match.group(2)
            mem_multiplier = {"KiB": 1 / 1024, "MiB": 1, "GiB": 1024}[mem_unit]
            mem_mib = mem_value * mem_multiplier

            rows.setdefault(name, []).append((cpu_value, mem_mib))

    summary = {}
    for name, samples in rows.items():
        cpus = [sample[0] for sample in samples]
        mems = [sample[1] for sample in samples]
        summary[name] = {
            "samples": len(samples),
            "cpu_avg": round(sum(cpus) / len(cpus), 2),
            "cpu_max": round(max(cpus), 2),
            "mem_avg_mib": round(sum(mems) / len(mems), 2),
            "mem_max_mib": round(max(mems), 2),
        }
    return summary


def main():
    if len(sys.argv) != 3:
        raise SystemExit("usage: summarize_async_answer_benchmark.py <k6-json> <docker-stats-csv>")

    json_path = Path(sys.argv[1])
    stats_path = Path(sys.argv[2])
    data = read_json(json_path)
    metrics = data["metrics"]

    result = {
        "http_req_duration": metric(metrics, "http_req_duration"),
        "answer_submit_202_ms": metric(metrics, "answer_submit_202_ms"),
        "submit_to_next_question_ws_ms": metric(metrics, "submit_to_next_question_ws_ms"),
        "iteration_duration": metric(metrics, "iteration_duration"),
        "ws_session_duration": metric(metrics, "ws_session_duration"),
        "http_reqs": metric(metrics, "http_reqs"),
        "iterations": metric(metrics, "iterations"),
        "dropped_iterations": metric(metrics, "dropped_iterations"),
        "checks": metrics.get("checks"),
        "docker_stats": parse_docker_stats(stats_path),
    }
    print(json.dumps(result, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
