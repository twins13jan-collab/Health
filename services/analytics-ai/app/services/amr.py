"""AMR (antimicrobial resistance) surveillance aggregation."""
from __future__ import annotations

from collections import defaultdict

from ..models import AmrPairSummary, AmrSummaryRequest, AmrSummaryResponse


def summarise_amr(req: AmrSummaryRequest) -> AmrSummaryResponse:
    buckets: dict[tuple[str, str], dict[str, int]] = defaultdict(
        lambda: {"isolates": 0, "resistant": 0, "intermediate": 0, "susceptible": 0}
    )
    for rec in req.records:
        b = buckets[(rec.organism.strip(), rec.antibiotic.strip())]
        b["isolates"] += 1
        if rec.result == "R":
            b["resistant"] += 1
        elif rec.result == "I":
            b["intermediate"] += 1
        else:
            b["susceptible"] += 1

    pairs: list[AmrPairSummary] = []
    for (organism, antibiotic), b in sorted(buckets.items()):
        pct = round(b["resistant"] / b["isolates"] * 100.0, 1) if b["isolates"] else 0.0
        alert = pct >= req.alert_threshold_pct and b["isolates"] >= req.min_isolates_for_alert
        pairs.append(
            AmrPairSummary(
                organism=organism, antibiotic=antibiotic,
                isolates=b["isolates"], resistant=b["resistant"],
                intermediate=b["intermediate"], susceptible=b["susceptible"],
                resistance_pct=pct, alert=alert,
            )
        )
    # Highest resistance first — the order a surveillance officer reads.
    pairs.sort(key=lambda p: (-p.resistance_pct, -p.isolates))
    return AmrSummaryResponse(
        total_records=len(req.records),
        pairs=pairs,
        alerts=[p for p in pairs if p.alert],
        alert_threshold_pct=req.alert_threshold_pct,
    )
