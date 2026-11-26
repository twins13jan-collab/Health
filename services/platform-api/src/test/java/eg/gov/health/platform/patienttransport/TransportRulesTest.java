package eg.gov.health.platform.patienttransport;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class TransportRulesTest {
  @Test void providerMustBeVerifiedLicensedAndCapable() {
    var trip = LocalDate.parse("2026-11-25");
    assertTrue(TransportRules.providerUsable(true, LocalDate.parse("2027-06-01"), trip, false, false, "wheelchair", false));
    assertFalse(TransportRules.providerUsable(false, LocalDate.parse("2027-06-01"), trip, false, false, "wheelchair", false)); // unverified
    assertFalse(TransportRules.providerUsable(true, LocalDate.parse("2026-11-01"), trip, false, false, "wheelchair", false));  // licence expired
    assertFalse(TransportRules.providerUsable(true, LocalDate.parse("2027-06-01"), trip, true, false, "seated", false));       // oxygen needed, none carried
    assertFalse(TransportRules.providerUsable(true, LocalDate.parse("2027-06-01"), trip, false, false, "stretcher", false));   // stretcher needed, none fitted
  }
  @Test void longDelaysAreReviewable() {
    assertTrue(TransportRules.delayNeedsReview(45));
    assertFalse(TransportRules.delayNeedsReview(10));
  }
}
