import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

import 'models.dart';

/// Typed REST client for the Java core (Spring Boot) — the same API base
/// as the web frontend.
///
/// Base URL resolution order:
///   1. `--dart-define=API_BASE_URL=...` at run/build time,
///   2. default `http://10.0.2.2:8080` (Android emulator → host machine),
///      use `http://localhost:8080` on iOS simulator / desktop.
class ApiClient {
  ApiClient({http.Client? httpClient, FlutterSecureStorage? storage})
      : _http = httpClient ?? http.Client(),
        _storage = storage ?? const FlutterSecureStorage();

  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );

  static const _tokenKey = 'ehs.accessToken';

  final http.Client _http;
  final FlutterSecureStorage _storage;
  String? _token;

  Future<String?> get token async =>
      _token ??= await _storage.read(key: _tokenKey);

  Future<void> _saveToken(String? value) async {
    _token = value;
    if (value == null) {
      await _storage.delete(key: _tokenKey);
    } else {
      await _storage.write(key: _tokenKey, value: value);
    }
  }

  Future<Map<String, String>> _headers() async => {
        'Content-Type': 'application/json',
        if (await token != null) 'Authorization': 'Bearer ${await token}',
      };

  Future<dynamic> _request(
    String method,
    String path, {
    Map<String, dynamic>? body,
  }) async {
    final uri = Uri.parse('$baseUrl$path');
    final headers = await _headers();
    final encoded = body == null ? null : jsonEncode(body);
    final http.Response res = await switch (method) {
      'POST' => _http.post(uri, headers: headers, body: encoded),
      'PATCH' => _http.patch(uri, headers: headers, body: encoded),
      _ => _http.get(uri, headers: headers),
    }.timeout(const Duration(seconds: 10));
    if (res.statusCode < 200 || res.statusCode >= 300) {
      throw ApiException(res.statusCode, res.body);
    }
    if (res.body.isEmpty) return null;
    return jsonDecode(utf8.decode(res.bodyBytes));
  }

  // ------------------------------------------------------------------ auth
  /// OIDC-style login against POST /api/auth/login on the core.
  Future<LoginResponse> login({
    required String username,
    required String password,
    String? facilityCode,
  }) async {
    final json = await _request('POST', '/api/auth/login', body: {
      'username': username,
      'password': password,
      if (facilityCode != null && facilityCode.isNotEmpty)
        'facilityCode': facilityCode,
    }) as Map<String, dynamic>;
    final session = LoginResponse.fromJson(json);
    await _saveToken(session.accessToken);
    return session;
  }

  Future<void> logout() => _saveToken(null);

  // -------------------------------------------------------------- patients
  Future<List<Patient>> searchPatients(String query) async {
    final q = Uri.encodeQueryComponent(query);
    final json = await _request('GET', '/api/patients?q=$q') as List<dynamic>;
    return json
        .map((e) => Patient.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  // ---------------------------------------------------------------- cycles
  Future<List<CycleDefinition>> cycleDefinitions() async {
    final json =
        await _request('GET', '/api/cycle-engine/definitions') as List<dynamic>;
    return json
        .map((e) => CycleDefinition.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// My open cycle tasks (instances not in a terminal state).
  /// Generic envelope list read for module endpoints (batches 2-6):
  /// labs critical alerts, dispatch queue, notifications, dialysis.
  Future<List<Map<String, dynamic>>> moduleList(
      String path, String envelopeKey) async {
    final env = await _request('GET', path);
    final rows = (env as Map<String, dynamic>)[envelopeKey];
    if (rows is List) {
      return rows.whereType<Map<String, dynamic>>().toList();
    }
    return const [];
  }

  Future<List<CycleInstance>> myCycleTasks() async {
    final json =
        await _request('GET', '/api/cycle-engine/instances') as List<dynamic>;
    return json
        .map((e) => CycleInstance.fromJson(e as Map<String, dynamic>))
        .where((i) =>
            i.status != CycleStatus.closed &&
            i.status != CycleStatus.cancelled)
        .toList();
  }

  Future<CycleInstance> transition(
    String instanceId,
    CycleStatus to, {
    String? reason,
  }) async {
    final json = await _request(
      'POST',
      '/api/cycle-engine/instances/$instanceId/transition',
      body: {'toStatus': to.wire, 'reason': reason},
    ) as Map<String, dynamic>;
    return CycleInstance.fromJson(json);
  }

  Future<CycleInstance> escalate(String instanceId, String reason) async {
    final json = await _request(
      'POST',
      '/api/cycle-engine/instances/$instanceId/escalate',
      body: {'reason': reason},
    ) as Map<String, dynamic>;
    return CycleInstance.fromJson(json);
  }
}

class ApiException implements Exception {
  const ApiException(this.statusCode, this.body);
  final int statusCode;
  final String body;

  @override
  String toString() => 'ApiException($statusCode): $body';
}
