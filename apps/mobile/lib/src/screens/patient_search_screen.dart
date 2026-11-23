import 'package:flutter/material.dart';

import '../api_client.dart';
import '../models.dart';

/// MPI patient search: by Arabic name or 14-digit national ID.
/// Results show the minimum necessary for care (privacy by design);
/// opening a full record is an audited action on the core.
class PatientSearchScreen extends StatefulWidget {
  const PatientSearchScreen({super.key, required this.api});

  final ApiClient api;

  @override
  State<PatientSearchScreen> createState() => _PatientSearchScreenState();
}

class _PatientSearchScreenState extends State<PatientSearchScreen> {
  final _query = TextEditingController();
  List<Patient> _results = const [];
  bool _busy = false;
  String? _error;
  bool _searched = false;

  @override
  void dispose() {
    _query.dispose();
    super.dispose();
  }

  Future<void> _search() async {
    final q = _query.text.trim();
    if (q.isEmpty) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final results = await widget.api.searchPatients(q);
      if (!mounted) return;
      setState(() {
        _results = results;
        _searched = true;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _error = 'تعذّر البحث — تحقق من الاتصال بالخادم الأساسي');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _query,
                  decoration: const InputDecoration(
                    labelText: 'الاسم أو الرقم القومي',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.search),
                  ),
                  textInputAction: TextInputAction.search,
                  onSubmitted: (_) => _search(),
                ),
              ),
              const SizedBox(width: 10),
              FilledButton(
                onPressed: _busy ? null : _search,
                child: const Text('بحث'),
              ),
            ],
          ),
          if (_error != null)
            Padding(
              padding: const EdgeInsets.only(top: 12),
              child: Text(_error!,
                  style: TextStyle(color: Theme.of(context).colorScheme.error)),
            ),
          const SizedBox(height: 12),
          Expanded(
            child: _busy
                ? const Center(child: CircularProgressIndicator())
                : _results.isEmpty
                    ? Center(
                        child: Text(_searched
                            ? 'لا توجد نتائج مطابقة'
                            : 'ابحث بالاسم أو الرقم القومي (14 رقمًا)'),
                      )
                    : ListView.separated(
                        itemCount: _results.length,
                        separatorBuilder: (_, __) => const Divider(height: 1),
                        itemBuilder: (context, i) {
                          final p = _results[i];
                          return ListTile(
                            leading: const CircleAvatar(
                              child: Icon(Icons.person),
                            ),
                            title: Text(p.fullNameAr),
                            subtitle: Text(
                              'الرقم القومي: ${p.nationalId}\n'
                              'الميلاد: ${p.dateOfBirth} · ${p.governorate ?? '—'}'
                              '${p.bloodType != null ? ' · فصيلة الدم: ${p.bloodType}' : ''}',
                            ),
                            isThreeLine: true,
                            trailing: p.chronicConditions.isEmpty
                                ? null
                                : Chip(
                                    label: Text(p.chronicConditions.join('، ')),
                                  ),
                          );
                        },
                      ),
          ),
        ],
      ),
    );
  }
}
