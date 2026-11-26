package eg.gov.health.platform.tempworkforce;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class TempWorkforceRulesTest {
  @Test void assignmentNeedsVerifiedIdentityQualificationsAndValidLicence() {
    var shift = LocalDate.parse("2026-11-25");
    assertTrue(TempWorkforceRules.providerAssignable(true, true, LocalDate.parse("2027-01-01"), shift, "verified"));
    assertFalse(TempWorkforceRules.providerAssignable(false, true, LocalDate.parse("2027-01-01"), shift, "verified"));
    assertFalse(TempWorkforceRules.providerAssignable(true, true, LocalDate.parse("2026-11-01"), shift, "verified")); // licence expired before the shift
    assertFalse(TempWorkforceRules.providerAssignable(true, true, LocalDate.parse("2027-01-01"), shift, "pending"));
  }
  @Test void doubleAssignmentIsDetectedByOverlap() {
    var s1 = OffsetDateTime.parse("2026-11-25T08:00:00+02:00"); var e1 = s1.plusHours(12);
    assertTrue(TempWorkforceRules.overlaps(s1, e1, s1.plusHours(6), e1.plusHours(6)));
    assertFalse(TempWorkforceRules.overlaps(s1, e1, e1, e1.plusHours(8)));
  }
  @Test void weeklyHoursCapHolds() {
    var s = OffsetDateTime.parse("2026-11-25T08:00:00+02:00");
    assertTrue(TempWorkforceRules.withinWeeklyLimit(48, s, s.plusHours(12)));  // 60 exactly
    assertFalse(TempWorkforceRules.withinWeeklyLimit(52, s, s.plusHours(12))); // 64 > 60
    assertTrue(TempWorkforceRules.scopeMatches("تمريض عام، تمريض عناية", "تمريض عناية"));
    assertFalse(TempWorkforceRules.scopeMatches("تمريض عام", "تخدير"));
  }
}
