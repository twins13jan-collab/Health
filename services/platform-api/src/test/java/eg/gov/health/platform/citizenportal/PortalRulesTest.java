package eg.gov.health.platform.citizenportal;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class PortalRulesTest {
  private final LocalDate today = LocalDate.of(2026, 11, 22);
  @Test void selfAccessNeedsVerifiedActiveIdentity() {
    assertTrue(PortalRules.selfAccess(true, "active"));
    assertFalse(PortalRules.selfAccess(false, "active"));
    assertFalse(PortalRules.selfAccess(true, "suspended"));
  }
  @Test void representativeScopeIsEnforced() {
    var until = LocalDate.of(2027, 1, 1);
    assertTrue(PortalRules.representativeAccess(true, until, "results", "lab_result", today));
    assertFalse(PortalRules.representativeAccess(true, until, "results", "prescription", today)); // scope mismatch
    assertTrue(PortalRules.representativeAccess(true, until, "all", "prescription", today));
    assertFalse(PortalRules.representativeAccess(false, until, "all", "prescription", today)); // unapproved
    assertFalse(PortalRules.representativeAccess(true, LocalDate.of(2026, 1, 1), "all", "prescription", today)); // expired
  }
  @Test void messagesFollowChannelConsent() {
    assertTrue(PortalRules.channelConsented(true, true, false, false));
    assertFalse(PortalRules.channelConsented(true, true, true, false)); // clinical alert without clinical consent
    assertTrue(PortalRules.channelConsented(true, true, true, true));
    assertFalse(PortalRules.channelConsented(false, true, false, true)); // no preference on the channel
    assertFalse(PortalRules.channelConsented(true, false, false, true)); // channel disabled
  }
}
