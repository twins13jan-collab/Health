package eg.gov.health.platform.governance;
import org.junit.jupiter.api.Test; import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
class ConsentRulesTest {
  ConsentRules.ConsentView active() { return new ConsentRules.ConsentView("disclosure","insurer-x","summary","active", OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(30)); }
  @Test void exactScopeAllowed() { assertTrue(ConsentRules.isShareAllowed(active(), "disclosure","insurer-x","summary", OffsetDateTime.now())); }
  @Test void differentPurposeDenied() { assertFalse(ConsentRules.isShareAllowed(active(), "research","insurer-x","summary", OffsetDateTime.now())); }
  @Test void revokedDenied() {
    var c = new ConsentRules.ConsentView("disclosure","insurer-x","summary","revoked", OffsetDateTime.now().minusDays(1), null);
    assertFalse(ConsentRules.isShareAllowed(c, "disclosure","insurer-x","summary", OffsetDateTime.now()));
  }
  @Test void expiredDenied() {
    var c = new ConsentRules.ConsentView("disclosure","insurer-x","summary","active", OffsetDateTime.now().minusDays(10), OffsetDateTime.now().minusDays(1));
    assertFalse(ConsentRules.isShareAllowed(c, "disclosure","insurer-x","summary", OffsetDateTime.now()));
  }
  @Test void breakGlassNeedsReasonAndBoundedDuration() {
    assertTrue(ConsentRules.isBreakGlassJustifiable("unconscious patient, allergy unknown", 60));
    assertFalse(ConsentRules.isBreakGlassJustifiable("urgent", 60));
    assertFalse(ConsentRules.isBreakGlassJustifiable("unconscious patient, allergy unknown", 999));
  }
}
