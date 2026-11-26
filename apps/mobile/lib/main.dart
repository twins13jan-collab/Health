import 'package:flutter/material.dart';

import 'src/api_client.dart';
import 'src/screens/login_screen.dart';

/// Egypt National Health System — mobile client (Catalog v6 rebuild).
///
/// Arabic-first RTL. One shared [ApiClient] instance talks to the Java
/// core over REST; the access token is kept in secure storage.
void main() {
  runApp(EgyptHealthApp(api: ApiClient()));
}

class EgyptHealthApp extends StatelessWidget {
  const EgyptHealthApp({super.key, required this.api});

  final ApiClient api;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'خطاب للصحة العامة وإدارة الأزمات',
      debugShowCheckedModeBanner: false,
      locale: const Locale('ar', 'EG'),
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF005463)),
        useMaterial3: true,
        fontFamily: 'NotoKufiArabic',
      ),
      // Arabic is RTL by default in Flutter once the locale is Arabic.
      home: LoginScreen(api: api),
    );
  }
}
