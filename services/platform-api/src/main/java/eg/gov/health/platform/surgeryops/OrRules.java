package eg.gov.health.platform.surgeryops;
import java.time.OffsetDateTime;
import java.util.Comparator;

/** Pure rules of theatre operations: waiting-list order, room
 *  separation, and the anaesthesia gates on starting a case. */
public final class OrRules {
  private OrRules() {}
  /** Minimum minutes between two cases in the same room
   *  (turnaround: cleaning, setup, anaesthesia induction). */
  public static final int ROOM_SEPARATION_MINUTES = 60;

  public static int priorityRank(String priority) {
    if (priority == null) return 2;
    return switch (priority) {
      case "emergency" -> 0;
      case "urgent" -> 1;
      default -> 2; // elective and anything else
    };
  }

  public static Comparator<SurgicalWaitlistEntry> waitlistOrder() {
    return Comparator.comparingInt((SurgicalWaitlistEntry e) -> priorityRank(e.getPriority()))
        .thenComparing(SurgicalWaitlistEntry::getCreatedAt);
  }

  /** A room conflict: another non-cancelled case in the same room
   *  whose start is closer than the separation window. */
  public static boolean roomConflict(OffsetDateTime candidate, java.util.List<OffsetDateTime> existingStarts) {
    if (candidate == null) return false;
    for (var t : existingStarts) {
      if (t == null) continue;
      long minutes = Math.abs(java.time.Duration.between(candidate, t).toMinutes());
      if (minutes < ROOM_SEPARATION_MINUTES) return true;
    }
    return false;
  }

  public static String anaesthesiaProblem(Integer asaClass, String type) {
    if (asaClass != null && (asaClass < 1 || asaClass > 5)) return "تصنيف ASA بين 1 و5";
    if (type != null && !java.util.Set.of("general", "spinal", "epidural", "local", "sedation").contains(type))
      return "نوع تخدير غير معروف: " + type;
    return null;
  }

  /** Starting a case needs its anaesthesia record with a completed
   *  pre-operative assessment — for elective and urgent alike; a
   *  true emergency may start without it but the override is the
   *  caller's documented decision, not a silent skip. */
  public static String startProblem(String caseStatus, boolean hasAnaesthesia, boolean preopDone, String priority) {
    if (!"scheduled".equals(caseStatus)) return "الحالة ليست مجدولة (الحالة الحالية: " + caseStatus + ")";
    if (!hasAnaesthesia) return "لا يوجد سجل تخدير للحالة بعد";
    if (!preopDone && !"emergency".equals(priority)) return "التقييم قبل الجراحة غير مكتمل — لا تبدأ حالة اختيارية أو عاجلة بدونه";
    return null;
  }

  public static String reasonProblem(String reason) {
    return (reason == null || reason.isBlank()) ? "السبب إلزامي وموثق للتأجيل أو الإلغاء" : null;
  }
}
