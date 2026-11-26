package eg.gov.health.platform.referralsdeep;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ReferralRulesTest {
  @Test void completeReferralsAndFairOrder() {
    assertFalse(ReferralRules.submittable("ألم صدر", false));
    assertTrue(ReferralRules.submittable("ألم صدر", true));
    assertFalse(ReferralRules.rejectionComplete("لا طاقة", ""));
    assertTrue(ReferralRules.rejectionComplete("لا طاقة", "عيادة المحافظة المجاورة"));
    var older = LocalDate.parse("2026-10-01"); var newer = LocalDate.parse("2026-11-01");
    assertTrue(ReferralRules.compareWaitlist(5, newer, 3, older) < 0);  // priority wins
    assertTrue(ReferralRules.compareWaitlist(3, older, 3, newer) < 0);  // then waiting time
  }
}
