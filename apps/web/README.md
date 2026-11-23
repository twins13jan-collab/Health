# Web — Egypt National Health System (Catalog v6 rebuild)

TypeScript + React 18 + Next.js 14 (App Router), Arabic-first RTL with an
English toggle. Talks to the Java core (Spring Boot) over REST via the typed
client in `lib/api.ts`.

## Run

```bash
npm install
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 npm run dev   # http://localhost:3000
npm run build && npm start                                    # production
```

`NEXT_PUBLIC_API_BASE_URL` defaults to `http://localhost:8080`. When the core
is unreachable, reads fall back to typed demo data and a banner says so —
writes are flagged as demo as well, never silently faked.

## Pages

| Route | Purpose |
|---|---|
| `/` | National command dashboard: KPI cards + governorate capacity table |
| `/patients` | MPI patient registry: search + registration form (14-digit national ID) |
| `/encounters` | Clinical encounters: triage board, filters, NEWS2 |
| `/cycles` | Operating-cycle engine (cycles 217–254): definitions, instance creation, allowed-transition buttons only |
| `/facilities` | Facilities registry with bed/ICU occupancy |
| `/login` | OIDC-style login form → `POST /api/auth/login` on the core |

Design system: `app/globals.css` (CSS variables, RTL, Arabic font stack, no
external CSS frameworks). Reusable components: `components/KpiCard.tsx`,
`components/DataTable.tsx`, `components/StatusBadge.tsx`, `components/Sidebar.tsx`.
Domain types: `lib/types.ts`.
