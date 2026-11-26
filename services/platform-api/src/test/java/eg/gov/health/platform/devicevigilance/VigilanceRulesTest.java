package eg.gov.health.platform.devicevigilance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VigilanceRulesTest {
  @Test void seriousIncidentsCarryRegulatoryDuties() {
    assertTrue(VigilanceRules.regulatoryReportRequired("serious", "harm"));
    assertTrue(VigilanceRules.regulatoryReportRequired("death", "harm"));
    assertFalse(VigilanceRules.regulatoryReportRequired("minor", "malfunction"));
  }
  @Test void closureWaitsForDutiesAndActions() {
    assertFalse(VigilanceRules.caseClosable(true, false, "completed")); // regulatory report unsent
    assertTrue(VigilanceRules.caseClosable(true, true, "completed"));
    assertFalse(VigilanceRules.caseClosable(false, false, "open"));     // field action still open
    assertTrue(VigilanceRules.caseClosable(false, false, "none"));
    assertEquals("recalled", VigilanceRules.trackingStatusFor("recall"));
    assertEquals("quarantined", VigilanceRules.trackingStatusFor("quarantine"));
  }
}
