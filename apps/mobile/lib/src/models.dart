/// Core domain models mirroring the Java core (Spring Boot) REST contracts.
/// Catalog v6 — same field names as the web frontend's `lib/types.ts`.
library;

class Patient {
  const Patient({
    required this.id,
    required this.nationalId,
    required this.fullNameAr,
    this.fullNameEn,
    required this.dateOfBirth,
    required this.gender,
    this.governorate,
    this.phone,
    this.bloodType,
    this.chronicConditions = const [],
  });

  final String id;

  /// Egyptian national ID (الرقم القومي) — the MPI linkage key.
  final String nationalId;
  final String fullNameAr;
  final String? fullNameEn;
  final String dateOfBirth; // ISO date
  final String gender; // male | female
  final String? governorate;
  final String? phone;
  final String? bloodType;
  final List<String> chronicConditions;

  factory Patient.fromJson(Map<String, dynamic> json) => Patient(
        id: json['id'] as String,
        nationalId: json['nationalId'] as String,
        fullNameAr: json['fullNameAr'] as String,
        fullNameEn: json['fullNameEn'] as String?,
        dateOfBirth: json['dateOfBirth'] as String,
        gender: json['gender'] as String,
        governorate: json['governorate'] as String?,
        phone: json['phone'] as String?,
        bloodType: json['bloodType'] as String?,
        chronicConditions: ((json['chronicConditions'] as List<dynamic>?) ?? [])
            .map((e) => e as String)
            .toList(),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'nationalId': nationalId,
        'fullNameAr': fullNameAr,
        'fullNameEn': fullNameEn,
        'dateOfBirth': dateOfBirth,
        'gender': gender,
        'governorate': governorate,
        'phone': phone,
        'bloodType': bloodType,
        'chronicConditions': chronicConditions,
      };
}

/// Operating-cycle lifecycle, Catalog v6 mandatory specification method.
enum CycleStatus {
  draft,
  open,
  inProgress,
  onHold,
  pendingReview,
  completed,
  closed,
  cancelled,
  failed,
  corrected,
  reconciled;

  static CycleStatus fromWire(String wire) => switch (wire) {
        'in_progress' => CycleStatus.inProgress,
        'on_hold' => CycleStatus.onHold,
        'pending_review' => CycleStatus.pendingReview,
        _ => CycleStatus.values.firstWhere(
            (s) => s.name == wire,
            orElse: () => CycleStatus.draft,
          ),
      };

  String get wire => switch (this) {
        CycleStatus.inProgress => 'in_progress',
        CycleStatus.onHold => 'on_hold',
        CycleStatus.pendingReview => 'pending_review',
        _ => name,
      };
}

/// Allowed transitions — mirror of the unified state machine in the core.
const Map<CycleStatus, List<CycleStatus>> cycleTransitions = {
  CycleStatus.draft: [CycleStatus.open],
  CycleStatus.open: [CycleStatus.inProgress, CycleStatus.cancelled],
  CycleStatus.inProgress: [
    CycleStatus.onHold,
    CycleStatus.pendingReview,
    CycleStatus.failed,
  ],
  CycleStatus.onHold: [CycleStatus.inProgress],
  CycleStatus.pendingReview: [CycleStatus.inProgress, CycleStatus.completed],
  CycleStatus.completed: [CycleStatus.closed, CycleStatus.corrected],
  CycleStatus.corrected: [CycleStatus.closed],
  CycleStatus.failed: [CycleStatus.reconciled],
  CycleStatus.reconciled: [CycleStatus.closed],
  CycleStatus.cancelled: [CycleStatus.reconciled],
  CycleStatus.closed: [],
};

/// Transitions that require a documented reason (Catalog v6).
const Set<CycleStatus> reasonRequired = {
  CycleStatus.cancelled,
  CycleStatus.failed,
  CycleStatus.corrected,
  CycleStatus.reconciled,
  CycleStatus.onHold,
};

class CycleDefinition {
  const CycleDefinition({
    required this.code,
    required this.catalogNumber,
    required this.titleAr,
    this.titleEn,
    required this.domain,
    this.specialisedRoute,
    required this.slaHours,
    required this.status,
  });

  final String code; // e.g. CYCLE-217
  final int catalogNumber; // 217..254
  final String titleAr;
  final String? titleEn;
  final String domain;
  final String? specialisedRoute;
  final int slaHours;
  final String status;

  factory CycleDefinition.fromJson(Map<String, dynamic> json) =>
      CycleDefinition(
        code: json['code'] as String,
        catalogNumber: (json['catalogNumber'] as num).toInt(),
        titleAr: json['titleAr'] as String,
        titleEn: json['titleEn'] as String?,
        domain: json['domain'] as String,
        specialisedRoute: json['specialisedRoute'] as String?,
        slaHours: (json['slaHours'] as num).toInt(),
        status: json['status'] as String,
      );
}

class CycleInstance {
  const CycleInstance({
    required this.id,
    required this.facilityId,
    required this.definitionCode,
    required this.title,
    required this.status,
    this.currentStep,
    this.protocolVersion,
    required this.correlationId,
    this.slaDueAt,
    required this.escalated,
    required this.createdAt,
    required this.updatedAt,
  });

  final String id;
  final String facilityId;
  final String definitionCode;
  final String title;
  final CycleStatus status;
  final String? currentStep;
  final String? protocolVersion;
  final String correlationId;
  final DateTime? slaDueAt;
  final bool escalated;
  final DateTime createdAt;
  final DateTime updatedAt;

  List<CycleStatus> get allowedTransitions =>
      cycleTransitions[status] ?? const [];

  bool get slaBreached =>
      slaDueAt != null &&
      DateTime.now().isAfter(slaDueAt!) &&
      status != CycleStatus.closed &&
      status != CycleStatus.cancelled &&
      status != CycleStatus.completed;

  factory CycleInstance.fromJson(Map<String, dynamic> json) => CycleInstance(
        id: json['id'] as String,
        facilityId: json['facilityId'] as String,
        definitionCode: json['definitionCode'] as String,
        title: json['title'] as String,
        status: CycleStatus.fromWire(json['status'] as String),
        currentStep: json['currentStep'] as String?,
        protocolVersion: json['protocolVersion'] as String?,
        correlationId: json['correlationId'] as String,
        slaDueAt: json['slaDueAt'] == null
            ? null
            : DateTime.parse(json['slaDueAt'] as String),
        escalated: json['escalated'] as bool? ?? false,
        createdAt: DateTime.parse(json['createdAt'] as String),
        updatedAt: DateTime.parse(json['updatedAt'] as String),
      );
}

class SessionUser {
  const SessionUser({
    required this.id,
    required this.fullNameAr,
    required this.role,
    this.facilityId,
  });

  final String id;
  final String fullNameAr;
  final String role;
  final String? facilityId;

  factory SessionUser.fromJson(Map<String, dynamic> json) => SessionUser(
        id: json['id'] as String,
        fullNameAr: json['fullNameAr'] as String,
        role: json['role'] as String,
        facilityId: json['facilityId'] as String?,
      );
}

class LoginResponse {
  const LoginResponse({
    required this.accessToken,
    required this.tokenType,
    required this.expiresInSeconds,
    required this.user,
  });

  final String accessToken;
  final String tokenType;
  final int expiresInSeconds;
  final SessionUser user;

  factory LoginResponse.fromJson(Map<String, dynamic> json) => LoginResponse(
        accessToken: json['accessToken'] as String,
        tokenType: json['tokenType'] as String? ?? 'Bearer',
        expiresInSeconds: (json['expiresInSeconds'] as num?)?.toInt() ?? 3600,
        user: SessionUser.fromJson(json['user'] as Map<String, dynamic>),
      );
}
