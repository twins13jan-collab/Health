package eg.gov.health.platform.offline;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class OfflineRulesTest {
  @Test void packageUsability() {
    var now = OffsetDateTime.now();
    assertTrue(OfflineRules.packageUsable("active", now.plusDays(1), now, "trusted", true));
    assertFalse(OfflineRules.packageUsable("active", now.minusDays(1), now, "trusted", true)); // expired
    assertFalse(OfflineRules.packageUsable("active", now.plusDays(1), now, "revoked", true)); // lost device
    assertFalse(OfflineRules.packageUsable("active", now.plusDays(1), now, "trusted", false)); // unencrypted
    assertFalse(OfflineRules.packageUsable("revoked", now.plusDays(1), now, "trusted", true));
  }
  @Test void staleReferenceHaltsFreshChecksOnly() {
    assertFalse(OfflineRules.freshEnough("dispense", 1));
    assertTrue(OfflineRules.freshEnough("dispense", OfflineRules.CURRENT_REFERENCE_VERSION));
    assertTrue(OfflineRules.freshEnough("appointment_note", 1)); // non-verification op proceeds
  }
  @Test void clinicalFinancialNeverAutoResolve() {
    assertFalse(OfflineRules.autoResolvable("clinical"));
    assertFalse(OfflineRules.autoResolvable("financial"));
    assertTrue(OfflineRules.autoResolvable("administrative"));
  }
  @Test void scopeIsABoundNotAnExpansion() {
    assertTrue(OfflineRules.withinScope("visit_note,vitals", "vitals"));
    assertFalse(OfflineRules.withinScope("visit_note,vitals", "payment"));
    assertTrue(OfflineRules.withinScope("*", "payment"));
  }
}
