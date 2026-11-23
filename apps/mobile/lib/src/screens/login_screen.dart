import 'package:flutter/material.dart';

import '../api_client.dart';
import 'home_screen.dart';

/// OIDC-style login: username/password (+ optional facility code) exchanged
/// for an OAuth 2.1 access token at POST /api/auth/login on the Java core.
/// MFA (TOTP) challenges are surfaced by the core as a 401 with a
/// `mfa_required` body, shown to the user as an error message here.
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key, required this.api});

  final ApiClient api;

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _username = TextEditingController();
  final _password = TextEditingController();
  final _facilityCode = TextEditingController();
  bool _busy = false;
  String? _error;

  @override
  void dispose() {
    _username.dispose();
    _password.dispose();
    _facilityCode.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final session = await widget.api.login(
        username: _username.text.trim(),
        password: _password.text,
        facilityCode: _facilityCode.text.trim(),
      );
      if (!mounted) return;
      Navigator.of(context).pushReplacement(
        MaterialPageRoute<void>(
          builder: (_) => HomeScreen(api: widget.api, user: session.user),
        ),
      );
    } on ApiException catch (e) {
      setState(() => _error = e.statusCode == 401
          ? 'بيانات الدخول غير صحيحة أو يلزم تحقق إضافي (MFA)'
          : 'تعذّر الاتصال بالخادم (${e.statusCode})');
    } catch (_) {
      setState(() => _error = 'تعذّر الاتصال بالخادم الأساسي — تحقق من الشبكة');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('تسجيل الدخول')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Icon(Icons.local_hospital, size: 56),
                const SizedBox(height: 12),
                Text(
                  'المنظومة الوطنية لإدارة الأزمات الصحية والطوارئ الطبية',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 24),
                TextFormField(
                  controller: _username,
                  decoration: const InputDecoration(
                    labelText: 'اسم المستخدم / البريد الرسمي',
                    border: OutlineInputBorder(),
                  ),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'حقل مطلوب' : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _password,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: 'كلمة المرور',
                    border: OutlineInputBorder(),
                  ),
                  validator: (v) =>
                      (v == null || v.isEmpty) ? 'حقل مطلوب' : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _facilityCode,
                  decoration: const InputDecoration(
                    labelText: 'كود المنشأة (اختياري)',
                    border: OutlineInputBorder(),
                  ),
                ),
                if (_error != null) ...[
                  const SizedBox(height: 12),
                  Text(_error!,
                      style: TextStyle(color: Theme.of(context).colorScheme.error)),
                ],
                const SizedBox(height: 20),
                FilledButton(
                  onPressed: _busy ? null : _submit,
                  child: _busy
                      ? const SizedBox(
                          height: 18,
                          width: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('دخول'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
