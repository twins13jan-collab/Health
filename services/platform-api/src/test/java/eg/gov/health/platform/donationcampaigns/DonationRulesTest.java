package eg.gov.health.platform.donationcampaigns;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class DonationRulesTest {
  @Test void ninetyDayIntervalIsRespected() {
    var campaign = LocalDate.parse("2026-11-22");
    assertTrue(DonationRules.intervalRespected(LocalDate.parse("2026-08-01"), campaign));  // 113 days
    assertFalse(DonationRules.intervalRespected(LocalDate.parse("2026-10-15"), campaign)); // 38 days
    assertTrue(DonationRules.intervalRespected(null, campaign));
  }
  @Test void eligibilityIsAllChecksTogether() {
    assertTrue(DonationRules.donorEligible(true, true, true, true, true));
    assertFalse(DonationRules.donorEligible(true, true, false, true, true)); // haemoglobin
    assertFalse(DonationRules.donorEligible(true, true, true, true, false)); // interval
  }
  @Test void allocationNeverExceedsCollection() {
    assertTrue(DonationRules.allocationPermitted(30, 20, 50));
    assertFalse(DonationRules.allocationPermitted(30, 25, 50));
  }
}
