package eg.gov.health.platform.cardiology;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class CardiologyRulesTest {
  private OffsetDateTime t(String iso) { return OffsetDateTime.parse(iso); }
  @Test void doorToBalloonTarget() {
    assertEquals(80, CardiologyRules.doorToBalloonMinutes(t("2026-11-22T08:00+02:00"), t("2026-11-22T09:20+02:00")));
    assertFalse(CardiologyRules.doorToBalloonBreached(t("2026-11-22T08:00+02:00"), t("2026-11-22T09:20+02:00")));
    assertTrue(CardiologyRules.doorToBalloonBreached(t("2026-11-22T08:00+02:00"), t("2026-11-22T10:05+02:00"))); // 125 min breach is flagged
  }
  @Test void preparationChecklistIsCompleteOrNothing() {
    assertTrue(CardiologyRules.preparationComplete(true, true, "normal", true, true));
    assertFalse(CardiologyRules.preparationComplete(false, true, "normal", true, true)); // no consent
    assertFalse(CardiologyRules.preparationComplete(true, true, null, true, true)); // renal function unrecorded
    assertFalse(CardiologyRules.preparationComplete(true, true, "normal", false, true)); // antiplatelet not loaded
    assertFalse(CardiologyRules.preparationComplete(true, true, "normal", true, false)); // team unconfirmed
  }
  @Test void traceabilityAndAkiWatch() {
    assertFalse(CardiologyRules.implantTraceable(null));
    assertTrue(CardiologyRules.implantTraceable("LOT-DES-2211"));
    assertTrue(CardiologyRules.akiWatch(260, "impaired baseline"));
    assertFalse(CardiologyRules.akiWatch(260, "normal"));
    assertFalse(CardiologyRules.akiWatch(80, "impaired baseline"));
  }
}
