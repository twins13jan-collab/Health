package eg.gov.health.platform.outpatientdeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class OutpatientRulesTest {
  @Test void closureNeedsTheFullCycle() {
    assertTrue(OutpatientRules.closureBlockers(true, true, true, "طبيب القلب").isEmpty());
    assertTrue(OutpatientRules.closureBlockers(true, true, true, null).contains("followup_owner_missing"));
    assertTrue(OutpatientRules.closureBlockers(false, true, true, "x").contains("identity_unverified"));
    assertTrue(OutpatientRules.closureBlockers(true, true, false, "x").contains("plan_undocumented"));
  }
}
