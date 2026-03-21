"""
query_analytics.py — Analytics queries for the shobdo Analytics collection.

Indexes ensured on startup:
  { ts: 1 }                    — TTL + range scans over time windows
  { event: 1, ts: 1 }         — partition by event type, then narrow by date
  { ip: 1, ts: 1 }            — per-IP history, deduplicate unique visitors
  { word: 1, ts: 1 }          — top words within a date range

All queries accept an optional date range (start / end) and push the
date filter down to MongoDB so only the relevant partition is scanned.

Usage:
    cd scripts/python_enrichment
    poetry run python src/analytics/query_analytics.py --report all
    poetry run python src/analytics/query_analytics.py --report unique-visitors --days 7
    poetry run python src/analytics/query_analytics.py --report top-words --days 30 --limit 20
    poetry run python src/analytics/query_analytics.py --report referrers --days 30
    poetry run python src/analytics/query_analytics.py --report daily-volume --days 14
    poetry run python src/analytics/query_analytics.py --mongo mongodb+srv://...
"""

from __future__ import annotations

import argparse
from datetime import datetime, timedelta, timezone
from typing import Optional

from pymongo import ASCENDING, MongoClient
from pymongo.collection import Collection

from src.logger import setup_logger

logger = setup_logger("analytics")

# ---------------------------------------------------------------------------
# Index bootstrap
# ---------------------------------------------------------------------------

INDEXES = [
    # Primary time-range scans (also used by TTL — matches the Java-side index)
    [("ts", ASCENDING)],
    # Queries partitioned by event type then narrowed to a date window
    [("event", ASCENDING), ("ts", ASCENDING)],
    # Per-IP deduplication within a time window
    [("ip", ASCENDING), ("ts", ASCENDING)],
    # Top-word queries within a time window
    [("word", ASCENDING), ("ts", ASCENDING)],
]


def ensure_indexes(col: Collection) -> None:
    for key_spec in INDEXES:
        col.create_index(key_spec, background=True)
    logger.info("Indexes ensured on Analytics collection")


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _date_range(days: int) -> tuple[datetime, datetime]:
    now = datetime.now(timezone.utc)
    return now - timedelta(days=days), now


def _ts_filter(start: datetime, end: datetime) -> dict:
    """Produces a $match stage that uses the { ts: 1 } index."""
    return {"ts": {"$gte": start, "$lte": end}}


# ---------------------------------------------------------------------------
# Queries
# ---------------------------------------------------------------------------

def unique_visitors(col: Collection, days: int = 30) -> list[dict]:
    """
    Unique IPs per day, partitioned by the { ts: 1 } index.
    Groups on (date, ip) first, then counts distinct IPs per date.
    """
    start, end = _date_range(days)
    pipeline = [
        {"$match": _ts_filter(start, end)},
        {"$group": {
            "_id": {
                "date": {"$dateToString": {"format": "%Y-%m-%d", "date": "$ts"}},
                "ip":   "$ip",
            }
        }},
        {"$group": {
            "_id":            "$_id.date",
            "unique_visitors": {"$sum": 1},
        }},
        {"$sort": {"_id": -1}},
    ]
    return list(col.aggregate(pipeline))


def daily_event_volume(col: Collection, days: int = 30) -> list[dict]:
    """
    Total events per day broken down by event type.
    Uses the { event: 1, ts: 1 } compound index via $match on ts.
    """
    start, end = _date_range(days)
    pipeline = [
        {"$match": _ts_filter(start, end)},
        {"$group": {
            "_id": {
                "date":  {"$dateToString": {"format": "%Y-%m-%d", "date": "$ts"}},
                "event": "$event",
            },
            "count": {"$sum": 1},
        }},
        {"$sort": {"_id.date": -1, "_id.event": 1}},
    ]
    return list(col.aggregate(pipeline))


def top_words(col: Collection, days: int = 30, limit: int = 20) -> list[dict]:
    """
    Most looked-up words within the date window.
    Uses the { word: 1, ts: 1 } compound index — only word_lookup events,
    status 200 (found words only, not 404 misses).
    """
    start, end = _date_range(days)
    pipeline = [
        {"$match": {**_ts_filter(start, end), "event": "word_lookup", "status": 200}},
        {"$group": {"_id": "$word", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": limit},
    ]
    return list(col.aggregate(pipeline))


def top_searches(col: Collection, days: int = 30, limit: int = 20) -> list[dict]:
    """
    Most common search strings (the search bar input, pre-lookup).
    Uses the { event: 1, ts: 1 } index partition.
    """
    start, end = _date_range(days)
    pipeline = [
        {"$match": {**_ts_filter(start, end), "event": "search"}},
        {"$group": {"_id": "$word", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": limit},
    ]
    return list(col.aggregate(pipeline))


def referrer_breakdown(col: Collection, days: int = 30, limit: int = 20) -> list[dict]:
    """
    Where visitors are coming from. Strips the path component so
    https://www.google.com/search?q=... groups under google.com.
    """
    start, end = _date_range(days)
    pipeline = [
        {"$match": {**_ts_filter(start, end), "referrer": {"$nin": [None, ""]}}},
        # Extract just the scheme+host as the "source"
        {"$addFields": {
            "source": {
                "$let": {
                    "vars": {"parts": {"$split": ["$referrer", "/"]}},
                    "in":   {"$concat": [
                        {"$arrayElemAt": ["$$parts", 0]}, "//",
                        {"$arrayElemAt": ["$$parts", 2]},
                    ]},
                }
            }
        }},
        {"$group": {"_id": "$source", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": limit},
    ]
    return list(col.aggregate(pipeline))


def not_found_words(col: Collection, days: int = 30, limit: int = 20) -> list[dict]:
    """
    Words users looked up that returned 404 — good candidates for adding
    to the dictionary. Uses { event: 1, ts: 1 } partition + status filter.
    """
    start, end = _date_range(days)
    pipeline = [
        {"$match": {**_ts_filter(start, end), "event": "word_lookup", "status": 404}},
        {"$group": {"_id": "$word", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": limit},
    ]
    return list(col.aggregate(pipeline))


def ip_activity(col: Collection, days: int = 7, limit: int = 20) -> list[dict]:
    """
    Most active IPs — useful for spotting bots or heavy users.
    Uses the { ip: 1, ts: 1 } index.
    """
    start, end = _date_range(days)
    pipeline = [
        {"$match": _ts_filter(start, end)},
        {"$group": {"_id": "$ip", "requests": {"$sum": 1}}},
        {"$sort": {"requests": -1}},
        {"$limit": limit},
    ]
    return list(col.aggregate(pipeline))


# ---------------------------------------------------------------------------
# Printing
# ---------------------------------------------------------------------------

def _print_table(title: str, rows: list[dict], key_label: str, val_label: str,
                 key_field: str = "_id", val_field: str = "count") -> None:
    print(f"\n{'━' * 56}")
    print(f"  {title}")
    print(f"{'━' * 56}")
    if not rows:
        print("  (no data)")
        return
    for row in rows:
        key = row.get(key_field, "—")
        val = row.get(val_field, "—")
        print(f"  {str(key):<38}  {val_label}: {val}")


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

REPORTS = ["unique-visitors", "daily-volume", "top-words", "top-searches",
           "referrers", "not-found", "ip-activity", "all"]


def main() -> None:
    parser = argparse.ArgumentParser(description="Shobdo analytics queries")
    parser.add_argument("--mongo",   default="mongodb://localhost:27017",
                        help="MongoDB URI (default: mongodb://localhost:27017)")
    parser.add_argument("--db",      default="Dictionary")
    parser.add_argument("--report",  choices=REPORTS, default="all")
    parser.add_argument("--days",    type=int, default=30,
                        help="Lookback window in days (default: 30)")
    parser.add_argument("--limit",   type=int, default=20,
                        help="Max rows for ranked lists (default: 20)")
    args = parser.parse_args()

    with MongoClient(args.mongo) as client:
        col: Collection = client[args.db]["Analytics"]
        ensure_indexes(col)

        run_all = args.report == "all"

        if run_all or args.report == "unique-visitors":
            rows = unique_visitors(col, days=args.days)
            _print_table(
                f"Unique visitors per day (last {args.days}d)",
                rows, "Date", "unique IPs", val_field="unique_visitors"
            )

        if run_all or args.report == "daily-volume":
            rows = daily_event_volume(col, days=args.days)
            # Pivot for display
            from collections import defaultdict
            by_date: dict[str, dict] = defaultdict(dict)
            for r in rows:
                by_date[r["_id"]["date"]][r["_id"]["event"]] = r["count"]
            print(f"\n{'━' * 56}")
            print(f"  Daily event volume (last {args.days}d)")
            print(f"{'━' * 56}")
            for date in sorted(by_date, reverse=True):
                counts = by_date[date]
                parts = "  ".join(f"{k}={v}" for k, v in sorted(counts.items()))
                print(f"  {date}  {parts}")

        if run_all or args.report == "top-words":
            rows = top_words(col, days=args.days, limit=args.limit)
            _print_table(
                f"Top {args.limit} looked-up words (last {args.days}d, found only)",
                rows, "Word", "lookups"
            )

        if run_all or args.report == "top-searches":
            rows = top_searches(col, days=args.days, limit=args.limit)
            _print_table(
                f"Top {args.limit} search strings (last {args.days}d)",
                rows, "Search string", "searches"
            )

        if run_all or args.report == "referrers":
            rows = referrer_breakdown(col, days=args.days, limit=args.limit)
            _print_table(
                f"Top {args.limit} referrers (last {args.days}d)",
                rows, "Referrer", "requests"
            )

        if run_all or args.report == "not-found":
            rows = not_found_words(col, days=args.days, limit=args.limit)
            _print_table(
                f"Top {args.limit} missing words — 404s (last {args.days}d)",
                rows, "Word", "misses"
            )

        if run_all or args.report == "ip-activity":
            rows = ip_activity(col, days=args.days, limit=args.limit)
            _print_table(
                f"Top {args.limit} most active IPs (last {args.days}d)",
                rows, "IP", "requests", val_field="requests"
            )


if __name__ == "__main__":
    main()
