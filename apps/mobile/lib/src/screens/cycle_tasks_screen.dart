import 'package:flutter/material.dart';

import '../api_client.dart';
import '../models.dart';

/// Operating-cycle tasks: my facility's open instances, with transition
/// buttons generated ONLY from the allowed-transition map (Catalog v6:
/// no illegal transition, no skipping a mandatory step). Transitions that
/// require a documented reason prompt for it first.
class CycleTasksScreen extends StatefulWidget {
  const CycleTasksScreen({super.key, required this.api});

  final ApiClient api;

  @override
  State<CycleTasksScreen> createState() => _CycleTasksScreenState();
}

class _CycleTasksScreenState extends State<CycleTasksScreen> {
  List<CycleInstance> _tasks = const [];
  bool _busy = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final tasks = await widget.api.myCycleTasks();
      if (!mounted) return;
      setState(() => _tasks = tasks);
    } catch (_) {
      if (!mounted) return;
      setState(() => _error = 'تعذّر تحميل المهام — تحقق من الاتصال');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<String?> _askReason(CycleStatus to) async {
    final controller = TextEditingController();
    final reason = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('سبب الانتقال إلى ${to.wire}'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(
            labelText: 'السبب (إلزامي — كتالوج v6)',
            border: OutlineInputBorder(),
          ),
          maxLines: 3,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('إلغاء'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, controller.text.trim()),
            child: const Text('تأكيد'),
          ),
        ],
      ),
    );
    controller.dispose();
    return (reason == null || reason.isEmpty) ? null : reason;
  }

  Future<void> _transition(CycleInstance task, CycleStatus to) async {
    String? reason;
    if (reasonRequired.contains(to)) {
      reason = await _askReason(to);
      if (reason == null) return; // cancelled, or no documented reason
    }
    try {
      final updated = await widget.api.transition(task.id, to, reason: reason);
      if (!mounted) return;
      setState(() {
        _tasks = _tasks
            .map((t) => t.id == updated.id ? updated : t)
            .where((t) =>
                t.status != CycleStatus.closed &&
                t.status != CycleStatus.cancelled)
            .toList();
      });
    } on ApiException catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.statusCode == 409
              ? 'انتقال غير مسموح به في حالة الدورة الحالية'
              : 'فشل الانتقال (${e.statusCode})'),
        ),
      );
    }
  }

  Future<void> _escalate(CycleInstance task) async {
    final reason = await _askReason(CycleStatus.onHold);
    if (reason == null) return;
    try {
      final updated = await widget.api.escalate(task.id, reason);
      if (!mounted) return;
      setState(() {
        _tasks = _tasks.map((t) => t.id == updated.id ? updated : t).toList();
      });
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('فشل التصعيد')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_busy) return const Center(child: CircularProgressIndicator());
    if (_error != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(_error!),
            const SizedBox(height: 10),
            FilledButton(onPressed: _load, child: const Text('إعادة المحاولة')),
          ],
        ),
      );
    }
    if (_tasks.isEmpty) {
      return const Center(child: Text('لا توجد دورات عمل مفتوحة مسندة إليك'));
    }
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.builder(
        padding: const EdgeInsets.all(12),
        itemCount: _tasks.length,
        itemBuilder: (context, i) {
          final task = _tasks[i];
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Chip(label: Text(task.definitionCode)),
                      const SizedBox(width: 8),
                      Chip(
                        label: Text(task.status.wire),
                        backgroundColor: task.slaBreached
                            ? Theme.of(context).colorScheme.errorContainer
                            : null,
                      ),
                      if (task.escalated) const Icon(Icons.priority_high, color: Colors.red),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(task.title,
                      style: Theme.of(context).textTheme.titleSmall),
                  if (task.currentStep != null)
                    Text('الخطوة الحالية: ${task.currentStep}'),
                  if (task.slaDueAt != null)
                    Text(
                      'الموعد الأقصى (SLA): ${task.slaDueAt!.toLocal()}'
                      '${task.slaBreached ? ' — تم التجاوز!' : ''}',
                      style: task.slaBreached
                          ? TextStyle(color: Theme.of(context).colorScheme.error)
                          : null,
                    ),
                  const SizedBox(height: 10),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      for (final to in task.allowedTransitions)
                        OutlinedButton(
                          onPressed: () => _transition(task, to),
                          child: Text('→ ${to.wire}'),
                        ),
                      TextButton.icon(
                        onPressed: () => _escalate(task),
                        icon: const Icon(Icons.arrow_upward),
                        label: const Text('تصعيد'),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
