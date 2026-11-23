import 'package:flutter/material.dart';

import '../api_client.dart';

/// Ambulance dispatch queue — unassigned requests first (cycle 224).
class DispatchQueueScreen extends StatefulWidget {
  const DispatchQueueScreen({super.key, required this.api});

  final ApiClient api;

  @override
  State<DispatchQueueScreen> createState() => _DispatchQueueScreenState();
}

class _DispatchQueueScreenState extends State<DispatchQueueScreen> {
  late Future<List<Map<String, dynamic>>> _future;

  @override
  void initState() {
    super.initState();
    _future = widget.api.moduleList('/api/dispatch/queue', 'queue');
  }

  @override
  Widget build(BuildContext context) {
    return RefreshIndicator(
      onRefresh: () async {
        setState(() {
          _future = widget.api.moduleList('/api/dispatch/queue', 'queue');
        });
        await _future;
      },
      child: FutureBuilder<List<Map<String, dynamic>>>(
        future: _future,
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError) {
            return ListView(children: [
              Padding(
                padding: const EdgeInsets.all(24),
                child: Text('تعذر الوصول للنواة: ${snap.error}'),
              ),
            ]);
          }
          final rows = snap.data ?? const [];
          if (rows.isEmpty) {
            return ListView(children: const [
              Padding(padding: EdgeInsets.all(24), child: Text('لا توجد عناصر حاليًا')),
            ]);
          }
          return ListView.separated(
            itemCount: rows.length,
            separatorBuilder: (_, __) => const Divider(height: 1),
            itemBuilder: (context, i) {
              final r = rows[i];
              return ListTile(
                leading: const Icon(Icons.local_hospital),
                title: Text('${r['complaint'] ?? ''}'),
                subtitle: Text('القدرة المطلوبة: ${r['requiredCapability'] ?? '—'}'),
                trailing: Chip(label: Text('${r['status'] ?? ''}')),
              );
            },
          );
        },
      ),
    );
  }
}
