# National Health Strategy 2024–2030 — Platform Alignment

The platform operates inside the frame of Egypt's **National
Health Strategy 2024–2030** (Ministry of Health & Population with
WHO Egypt, published October 2024; booklet supplied by the
platform owner). This document records how the strategy is
applied in the system — not as decoration, but as reference data,
an organizing frame for capabilities, and a live measurement
layer.

## What was implemented

- **Reference data (V108)** — `ref_strategy_priorities` (the 7
  strategic priorities, verbatim) and `ref_strategy_goals` (the
  44 goals under them, verbatim), plus the strategy board's row
  in the access matrix.
- **Public transparency page `/strategy`** — vision («أن ينعم
  جميع المقيمين على أرض مصر بحياة صحية آمنة…»), mission, the 9
  guiding principles (العدالة، الشفافية، الكرامة، السرية،
  الملكية، الشراكة، الشمولية، التفاعل والمشاركة، المساءلة),
  every priority with its goals, and the platform capabilities
  that serve it — served anonymously from
  `GET /api/public/strategy` (strategy priority 5: transparency;
  priority 7: community awareness).
- **Monitoring board `GET /api/strategy/board`** — for signed-in
  leadership, each priority carries live indicators computed from
  the platform's own operational data (strategy priority 5,
  goal 6: strong digital M&E systems). The mapping of priorities
  → capabilities → indicator keys lives in
  `strategy/StrategyAlignment.java`. An indicator that cannot be
  computed from real data renders as unavailable — never as an
  invented number.

## Indicator honesty

The indicators are **operational proxies** measured on the
facilities and citizens actually using this platform (e.g.
patient share of issued invoices as a financial-protection
proxy; claims-settled percentage; governorates with an active
facility). They are **not** the strategy's official national
statistics or targets — those live in the strategy's executive
plan (الخطة التنفيذية ومؤشرات الأداء الرئيسية) and in national
accounts (e.g. out-of-pocket spending was 59.3% of current
health expenditure in FY2019/20 per the National Health
Accounts cited by the strategy). When the official KPI annex is
supplied, its targets can be loaded beside these measures
without changing the board's shape.

## Priority → platform coverage (summary)

| Priority | Served by (examples) |
|---|---|
| P1 UHC & access | insurance claims board, billing & financial protection, clinics, private-sector licensing, facility directory |
| P2 Life course | maternity, neonatal/NICU, nutrition, rehabilitation |
| P3 Disease prevention & control | mental health, ICD-10 coding, analytics, clinic programmes |
| P4 Health security | ER & triage, live command dashboard, outbreak/AMR analytics, CSSD |
| P5 Equity, governance, accountability | access matrix, settings, admin approvals, workflow governance, audit |
| P6 Digital health innovation | AI assistant, patient portal, offline work, Android field app |
| P7 Community participation | public find-&-book, my-health account, complaints follow-up |
