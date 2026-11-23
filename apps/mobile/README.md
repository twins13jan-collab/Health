# Mobile — Egypt National Health System (Catalog v6 rebuild)

Dart/Flutter client for facility staff, over the **same REST API** as the web
frontend (`apps/web`) and the Java core (Spring Boot).

## Screens

| Screen | File | Purpose |
|---|---|---|
| Login | `lib/src/screens/login_screen.dart` | OIDC-style login → `POST /api/auth/login`; token in `flutter_secure_storage` |
| Home | `lib/src/screens/home_screen.dart` | Tab shell (patient search / cycle tasks) + signed-in user + logout |
| Patient search | `lib/src/screens/patient_search_screen.dart` | MPI search by Arabic name or 14-digit national ID → `GET /api/patients?q=` |
| Cycle tasks | `lib/src/screens/cycle_tasks_screen.dart` | Open operating-cycle instances → `GET /api/cycle-engine/instances`; transition buttons generated **only** from the allowed-transition map, with a mandatory-reason dialog where Catalog v6 requires one, plus escalation |

Code layout: `lib/main.dart` (app + Arabic RTL theme), `lib/src/api_client.dart`
(typed REST client), `lib/src/models.dart` (Patient, CycleDefinition,
CycleInstance, CycleStatus + transition map — mirroring the core contracts).

## Run it

> The Flutter SDK is **not installed in the build VM that produced this
> scaffold**, so this code has not been compiled here. On a workstation with
> Flutter 3.19+ / Dart 3.3+:

```bash
cd apps/mobile
flutter pub get
# Android emulator (maps to the host machine's Java core on :8080):
flutter run
# iOS simulator / desktop, or a real device on the same network:
flutter run --dart-define=API_BASE_URL=http://localhost:8080
# Physical device: use the host's LAN address instead of localhost.
```

The API base URL defaults to `http://10.0.2.2:8080` (Android emulator →
host) and is overridden with `--dart-define=API_BASE_URL=...`.

## Notes

- Arabic is the default locale (`ar-EG`); Flutter renders RTL automatically.
- `analysis_options.yaml` enables `flutter_lints` with strict casts/inference —
  run `flutter analyze` after `flutter pub get`.
- Offline work / safe synchronisation (Catalog v6 cycle 237) is a core-side
  contract; this scaffold intentionally does not fake offline sync locally.
