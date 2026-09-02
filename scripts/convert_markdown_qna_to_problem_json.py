#!/usr/bin/env python3
import argparse
import json
import re
from pathlib import Path


ROOT_CATEGORY_MAP = {
    "Data Structures": "data_structures",
    "자료구조": "data_structures",
    "Operating System": "operating_system",
    "운영체제 (OS)": "operating_system",
    "Database": "database",
    "데이터베이스": "database",
    "Network": "network",
    "네트워크": "network",
}

SUBCATEGORY_MAP = {
    "Array & Linked List": "array_linked_list",
    "Queue & Stack": "queue_stack",
    "Hash Table & BST": "hash_table_bst",
    "Hash table & BST": "hash_table_bst",
    "Process & Thread": "process_thread",
    "Memory": "memory",
    "DB Structure & Design": "db_structure_design",
    "DB 구조 & 설계": "db_structure_design",
    "Transaction": "transaction",
    "Index": "index",
    "TCP/IP": "tcp_ip",
    "HTTP": "http",
    "Authorization": "authorization",
}

TOP_HEADING_RE = re.compile(r"^#\s+\d+\.\s+(.+?)\s*$")
SUB_HEADING_RE = re.compile(r"^##\s+(.+?)\s*$")
QUESTION_RE = re.compile(r"^###\s+Q\.\s+(.+?)\s*$")
FOLLOW_UP_RE = re.compile(r"^-\s+(?:Follow-up|꼬리 질문):\s+(.+?)\s*$")
ANSWER_RE = re.compile(r"^\s*>\s*(.+?)\s*$")


def slugify(text: str) -> str:
    normalized = text.strip().lower()
    normalized = normalized.replace("&", " and ")
    normalized = re.sub(r"[^a-z0-9]+", "_", normalized)
    normalized = re.sub(r"_+", "_", normalized).strip("_")
    if not normalized:
        raise ValueError(f"cannot slugify category title: {text!r}")
    return normalized


def resolve_root_category(title: str) -> str:
    if title in ROOT_CATEGORY_MAP:
        return ROOT_CATEGORY_MAP[title]
    raise ValueError(f"unknown root category title: {title}")


def resolve_subcategory(title: str) -> str:
    return SUBCATEGORY_MAP.get(title, slugify(title))


def infer_content_language(path: Path, explicit: str | None) -> str:
    if explicit:
        return explicit.upper()
    filename = path.name.lower()
    if "-en." in filename:
        return "EN"
    if "-ko." in filename:
        return "KO"
    raise ValueError("content language is ambiguous; pass --content-language KO|EN")


def build_problem(question: str, answer: str, category: str, difficulty: str, content_language: str) -> dict:
    return {
        "problem": question,
        "referenceAnswer": answer,
        "difficulty": difficulty,
        "category": category,
        "contentLanguage": content_language,
        "followUpProblems": [],
    }


def parse_markdown(path: Path, difficulty: str, content_language: str) -> list[dict]:
    problems: list[dict] = []
    current_root_category = None
    current_subcategory = None
    current_problem = None
    pending = None

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.rstrip()
        if not line:
            continue

        top_heading = TOP_HEADING_RE.match(line)
        if top_heading:
            current_root_category = resolve_root_category(top_heading.group(1))
            current_subcategory = None
            current_problem = None
            pending = None
            continue

        sub_heading = SUB_HEADING_RE.match(line)
        if sub_heading:
            if current_root_category is None:
                raise ValueError(f"subcategory without root category: {line}")
            current_subcategory = resolve_subcategory(sub_heading.group(1))
            current_problem = None
            pending = None
            continue

        question_match = QUESTION_RE.match(line)
        if question_match:
            if current_root_category is None or current_subcategory is None:
                raise ValueError(f"question without category context: {line}")
            category = f"computer_science:{current_root_category}:{current_subcategory}"
            current_problem = build_problem(
                question_match.group(1),
                "",
                category,
                difficulty,
                content_language,
            )
            problems.append(current_problem)
            pending = ("root_answer", current_problem)
            continue

        follow_up_match = FOLLOW_UP_RE.match(line)
        if follow_up_match:
            if current_problem is None:
                raise ValueError(f"follow-up without root question: {line}")
            follow_up = build_problem(
                follow_up_match.group(1),
                "",
                current_problem["category"],
                difficulty,
                content_language,
            )
            current_problem["followUpProblems"].append(follow_up)
            pending = ("follow_up_answer", follow_up)
            continue

        answer_match = ANSWER_RE.match(line)
        if answer_match and pending is not None:
            pending[1]["referenceAnswer"] = answer_match.group(1)
            pending = None
            continue

    missing_answers = [
        problem["problem"]
        for problem in problems
        if not problem["referenceAnswer"]
        or any(not follow_up["referenceAnswer"] for follow_up in problem["followUpProblems"])
    ]
    if missing_answers:
        preview = ", ".join(missing_answers[:5])
        raise ValueError(f"missing reference answers for parsed problems: {preview}")

    return problems


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("input_markdown", type=Path)
    parser.add_argument("output_json", type=Path)
    parser.add_argument("--difficulty", default="MEDIUM")
    parser.add_argument("--content-language", choices=["KO", "EN"])
    args = parser.parse_args()

    content_language = infer_content_language(args.input_markdown, args.content_language)
    problems = parse_markdown(args.input_markdown, args.difficulty.upper(), content_language)
    args.output_json.write_text(json.dumps(problems, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
