package eg.gov.health.platform.clinics;
import java.time.*; import java.util.*;
/** Pure scheduling rules for outpatient clinics (unit-tested):
 *  session sanity, slot arithmetic, booking eligibility and the
 *  waiting-list promotion order (urgent first, then arrival). */
public final class ClinicRules {
  private ClinicRules() {}

  /** Why a session definition is invalid, or null when it is valid. */
  public static String sessionProblem(LocalTime start, LocalTime end, int slotMinutes, int capacity) {
    if (start == null || end == null || !end.isAfter(start)) return "وقت انتهاء الجلسة يجب أن يكون بعد بدايتها";
    if (slotMinutes < 5 || slotMinutes > 120) return "مدة الخانة يجب أن تكون بين 5 و120 دقيقة";
    if (capacity < 1 || capacity > 500) return "سعة الجلسة يجب أن تكون بين 1 و500";
    long minutes = Duration.between(start, end).toMinutes();
    if (minutes < slotMinutes) return "الجلسة أقصر من خانة واحدة";
    return null;
  }

  /** Number of slots that fit the session grid (floor). */
  public static int slotCount(LocalTime start, LocalTime end, int slotMinutes) {
    return (int) (Duration.between(start, end).toMinutes() / slotMinutes);
  }

  /** Why a booking into a session is refused, or null when it may proceed. */
  public static String bookingProblem(String sessionStatus, LocalDate sessionDate, LocalDate today,
                                      long bookedCount, int capacity, boolean patientAlreadyBooked) {
    if ("cancelled".equals(sessionStatus)) return "الجلسة ملغاة";
    if ("closed".equals(sessionStatus)) return "الجلسة مغلقة";
    if (sessionDate == null || sessionDate.isBefore(today)) return "لا حجز في جلسة بتاريخ ماضٍ";
    if (patientAlreadyBooked) return "المريض محجوز أصلًا في هذه الجلسة";
    if (bookedCount >= capacity) return "الجلسة ممتلئة — أضف المريض لقائمة الانتظار";
    return null;
  }

  /** Promotion order: urgent before normal, then earliest arrival. */
  public static Comparator<ClinicWaitlistEntry> promotionOrder() {
    return Comparator
      .comparingInt((ClinicWaitlistEntry w) -> "urgent".equals(w.getPriority()) ? 0 : 1)
      .thenComparing(ClinicWaitlistEntry::getCreatedAt);
  }

  /** Cancellation of a live booking requires a documented reason. */
  public static String cancelProblem(String reason) {
    return (reason == null || reason.isBlank()) ? "سبب الإلغاء مطلوب" : null;
  }
}
