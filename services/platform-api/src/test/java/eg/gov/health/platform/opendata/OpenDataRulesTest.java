package eg.gov.health.platform.opendata;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class OpenDataRulesTest {
  @Test void assessedAnonymizedSuppressedDocumented() {
    assertTrue(OpenDataRules.publishBlockers(true, true, 25, true).isEmpty());
    assertTrue(OpenDataRules.publishBlockers(true, true, 3, true).contains("small_cells_unsuppressed"));
    assertTrue(OpenDataRules.publishBlockers(false, true, 25, true).contains("reidentification_risk_unassessed"));
    assertTrue(OpenDataRules.publishBlockers(true, false, 25, true).contains("not_anonymized"));
  }
}
