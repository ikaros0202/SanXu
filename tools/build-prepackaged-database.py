#!/usr/bin/env python3
"""Build and verify SanXu's Room prepackaged database from the approved seed."""

from __future__ import annotations

import argparse
import hashlib
import json
import sqlite3
import datetime
import re
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SEED = PROJECT_ROOT / "data" / "fc3d-seed.json"
DEFAULT_SCHEMA = (
    PROJECT_ROOT
    / "core" / "data"
    / "schemas"
    / "com.sanxu.data.local.SanxuDatabase"
    / "1.json"
)
DEFAULT_OUTPUT = (
    PROJECT_ROOT / "core" / "data" / "src" / "main" / "assets" / "database" / "sanxu.db"
)


def fingerprint(record: dict) -> str:
    payload = (
        f"{record['issue']}|{record['drawDate']}|"
        f"{''.join(str(digit) for digit in record['digits'])}|"
        f"{record['officialDetailUrl']}"
    )
    return hashlib.sha256(payload.encode("utf-8")).hexdigest().upper()


def validate_records(records: list[dict]) -> None:
    if not records:
        raise ValueError("Seed must not be empty")
    issues = [record["issue"] for record in records]
    if len(set(issues)) != len(issues):
        raise ValueError("Seed contains duplicate issues")
    if issues[0] != "2017001":
        raise ValueError(f"Unexpected seed range: {issues[0]}..{issues[-1]}")
    previous = None
    for record in records:
        issue = record["issue"]
        digits = record["digits"]
        if not re.fullmatch(r'20[0-9]{5}',issue) or not 1<=int(issue[4:])<=366:
            raise ValueError(f"Invalid issue: {issue}")
        if len(digits) != 3 or any(type(d) is not int or d not in range(10) for d in digits):
            raise ValueError(f"Invalid digits for {issue}: {digits}")
        date=datetime.date.fromisoformat(record['drawDate'])
        if str(date.year)!=issue[:4]: raise ValueError('Issue year differs from draw date')
        if previous:
            expected=int(previous['issue'][4:])+1 if previous['issue'][:4]==issue[:4] else 1
            if issue<=previous['issue'] or int(issue[4:])!=expected or record['drawDate']<=previous['drawDate']:
                raise ValueError('Seed must be ordered, continuous, and have increasing dates')
        previous=record


def build_database(seed_path: Path, schema_path: Path, output_path: Path) -> None:
    seed_document = json.loads(seed_path.read_text(encoding="utf-8"))
    records = seed_document["draws"]
    declared_range = seed_document["range"]
    validate_records(records)
    if declared_range != {
        "firstIssue": "2017001",
        "lastIssue": records[-1]['issue'],
        "count": len(records),
    }:
        raise ValueError(f"Unexpected declared seed range: {declared_range}")
    schema = json.loads(schema_path.read_text(encoding="utf-8"))["database"]

    output_path.parent.mkdir(parents=True, exist_ok=True)
    temporary=output_path.with_suffix('.building.db')
    if temporary.exists(): temporary.unlink()
    connection = sqlite3.connect(temporary)
    try:
        connection.execute("PRAGMA journal_mode=DELETE")
        connection.execute("PRAGMA foreign_keys=ON")
        for entity in schema["entities"]:
            connection.execute(
                entity["createSql"].replace("${TABLE_NAME}", entity["tableName"])
            )
            for index in entity.get("indices", []):
                connection.execute(
                    index["createSql"].replace("${TABLE_NAME}", entity["tableName"])
                )
        for query in schema["setupQueries"]:
            connection.execute(query)

        connection.executemany(
            """
            INSERT INTO draws(
                issue, drawDate, hundreds, tens, ones,
                officialDetailUrl, officialFingerprint
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            [
                (
                    record["issue"],
                    record["drawDate"],
                    record["digits"][0],
                    record["digits"][1],
                    record["digits"][2],
                    record["officialDetailUrl"],
                    fingerprint(record),
                )
                for record in records
            ],
        )
        connection.execute(
            """
            INSERT INTO sync_metadata(
                id, lastAttemptEpochMillis, lastSuccessEpochMillis,
                latestIssue, lastFailureType, correctedIssuesJson
            ) VALUES (1, NULL, NULL, ?, NULL, '[]')
            """,
            (records[-1]["issue"],),
        )
        connection.execute(f"PRAGMA user_version={schema['version']}")
        connection.commit()

        count = connection.execute("SELECT COUNT(*) FROM draws").fetchone()[0]
        first, last = connection.execute(
            "SELECT MIN(issue), MAX(issue) FROM draws"
        ).fetchone()
        integrity = connection.execute("PRAGMA integrity_check").fetchone()[0]
        if (count, first, last, integrity) != (
            len(records),
            "2017001",
            records[-1]['issue'],
            "ok",
        ):
            raise ValueError(
                f"Database verification failed: {count}, {first}, {last}, {integrity}"
            )
        connection.execute("VACUUM")
    finally:
        connection.close()

    temporary.replace(output_path)

    digest = hashlib.sha256(output_path.read_bytes()).hexdigest().upper()
    print(f"Created {output_path}")
    print(f"Records: {len(records)} (2017001..{records[-1]['issue']})")
    print(f"SHA-256: {digest}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--seed", type=Path, default=DEFAULT_SEED)
    parser.add_argument("--schema", type=Path, default=DEFAULT_SCHEMA)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    build_database(args.seed.resolve(), args.schema.resolve(), args.output.resolve())


if __name__ == "__main__":
    main()
