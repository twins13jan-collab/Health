package eg.gov.health.platform.masterdata;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MasterDataRulesTest {
  @Test void usageGate() {
    assertTrue(MasterDataRules.usableInNewTransaction("active", false));
    assertFalse(MasterDataRules.usableInNewTransaction("deprecated", false));
    assertTrue(MasterDataRules.usableInNewTransaction("deprecated", true)); // approved exception
    assertFalse(MasterDataRules.usableInNewTransaction("unknown_review", true));
  }
  @Test void unknownNeverGuessed() {
    assertFalse(MasterDataRules.mappingAllowed("unknown_review", false));
    assertTrue(MasterDataRules.mappingAllowed("unknown_review", true));
    assertTrue(MasterDataRules.mappingAllowed("active", false));
  }
  @Test void governanceDecides() {
    assertTrue(MasterDataRules.mayDecideStewardship("data_governance"));
    assertFalse(MasterDataRules.mayDecideStewardship("receptionist"));
    assertFalse(MasterDataRules.mayDecideStewardship("facility_manager"));
  }
}
