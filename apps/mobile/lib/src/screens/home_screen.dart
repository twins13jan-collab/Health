import 'package:flutter/material.dart';

import '../api_client.dart';
import '../models.dart';
import 'critical_alerts_screen.dart';
import 'cycle_tasks_screen.dart';
import 'dispatch_queue_screen.dart';
import 'login_screen.dart';
import 'patient_search_screen.dart';

/// Staff home: patient search (MPI) + operating-cycle tasks.
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key, required this.api, required this.user});

  final ApiClient api;
  final SessionUser user;

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 4,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('المنظومة الوطنية الصحية'),
          actions: [
            IconButton(
              icon: const Icon(Icons.logout),
              tooltip: 'تسجيل الخروج',
              onPressed: () async {
                await api.logout();
                if (!context.mounted) return;
                Navigator.of(context).pushReplacement(
                  MaterialPageRoute<void>(
                    builder: (_) => LoginScreen(api: api),
                  ),
                );
              },
            ),
          ],
          bottom: const TabBar(
            tabs: [
              Tab(icon: Icon(Icons.person_search), text: 'بحث المرضى'),
              Tab(icon: Icon(Icons.cached), text: 'مهام دورات العمل'),
              Tab(icon: Icon(Icons.warning_amber), text: 'نتائج حرجة'),
              Tab(icon: Icon(Icons.local_hospital), text: 'إرسال الإسعاف'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            PatientSearchScreen(api: api),
            CycleTasksScreen(api: api),
            CriticalAlertsScreen(api: api),
            DispatchQueueScreen(api: api),
          ],
        ),
        bottomNavigationBar: Material(
          color: Theme.of(context).colorScheme.surfaceContainerHighest,
          child: Padding(
            padding: const EdgeInsets.all(10),
            child: Text(
              '${user.fullNameAr} · ${user.role}',
              textAlign: TextAlign.center,
            ),
          ),
        ),
      ),
    );
  }
}
