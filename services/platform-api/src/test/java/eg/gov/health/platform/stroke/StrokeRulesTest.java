package eg.gov.health.platform.stroke;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class StrokeRulesTest {
  private OffsetDateTime t(String iso) { return OffsetDateTime.parse(iso); }
  @Test void thrombolysisWindowIsFourAndHalfHours() {
    var lkw = t("2026-11-22T06:00+02:00");
    assertTrue(StrokeRules.withinWindow(lkw, t("2026-11-22T10:30+02:00"))); // exactly 4.5h
    assertFalse(StrokeRules.withinWindow(lkw, t("2026-11-22T10:31+02:00")));
  }
  @Test void permissionNeedsCtCleanChecklistAndWindow() {
    var lkw = t("2026-11-22T06:00+02:00"); var needle = t("2026-11-22T09:00+02:00");
    assertTrue(StrokeRules.thrombolysisPermitted(true, false, lkw, needle));
    assertFalse(StrokeRules.thrombolysisPermitted(false, false, lkw, needle)); // hemorrhage not excluded
    assertFalse(StrokeRules.thrombolysisPermitted(true, true, lkw, needle)); // contraindication present
    assertTrue(StrokeRules.anyContraindication(false, true, false, false, false));
    assertFalse(StrokeRules.anyContraindication(false, false, false, false, false));
  }
  @Test void doorToNeedleAndSwallow() {
    assertTrue(StrokeRules.doorToNeedleBreached(t("2026-11-22T08:00+02:00"), t("2026-11-22T09:15+02:00")));
    assertFalse(StrokeRules.doorToNeedleBreached(t("2026-11-22T08:00+02:00"), t("2026-11-22T08:45+02:00")));
    assertTrue(StrokeRules.oralIntakePermitted("passed"));
    assertFalse(StrokeRules.oralIntakePermitted("not_done"));
    assertFalse(StrokeRules.oralIntakePermitted("failed"));
  }
}
