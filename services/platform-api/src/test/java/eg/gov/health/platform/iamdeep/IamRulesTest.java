package eg.gov.health.platform.iamdeep;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class IamRulesTest {
  @Test void noSelfGrantAndComputedExpiry() {
    var exp = OffsetDateTime.parse("2026-12-01T00:00:00+02:00");
    assertFalse(IamRules.grantable("دعم فني", "facility", "أحمد", "أحمد", true, exp)); // self-grant
    assertFalse(IamRules.grantable("", "facility", "أحمد", "مدير", true, exp));        // no purpose
    assertFalse(IamRules.grantable("دعم فني", "facility", "أحمد", "مدير", true, null)); // temporary without expiry
    assertTrue(IamRules.grantable("دعم فني", "facility", "أحمد", "مدير", true, exp));
    assertFalse(IamRules.active(true, exp, OffsetDateTime.parse("2026-12-02T00:00:00+02:00"))); // expired by computation
    assertTrue(IamRules.active(true, exp, OffsetDateTime.parse("2026-11-22T00:00:00+02:00")));
    assertTrue(IamRules.active(false, null, OffsetDateTime.now()));
  }
}
