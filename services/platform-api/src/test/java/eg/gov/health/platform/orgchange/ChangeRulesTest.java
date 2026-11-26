package eg.gov.health.platform.orgchange;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ChangeRulesTest {
  @Test void goLiveIsGatedOnPeopleNotTechnology() {
    assertTrue(ChangeRules.goLivePermitted("أثر على أدوار الاستقبال والفوترة", true, 85, 100, true));
    assertFalse(ChangeRules.goLivePermitted("تحليل", true, 60, 100, true));  // only 60% trained
    assertFalse(ChangeRules.goLivePermitted("تحليل", false, 90, 100, true)); // nobody told
    assertFalse(ChangeRules.goLivePermitted("تحليل", true, 90, 100, false)); // SOPs still describe the old world
    assertFalse(ChangeRules.goLivePermitted(null, true, 90, 100, true));     // no impact analysis
  }
  @Test void adoptionIsMeasuredNotDeclared() {
    assertTrue(ChangeRules.adoptionDeclared(78.0, 0));
    assertFalse(ChangeRules.adoptionDeclared(78.0, 3)); // workarounds = the organization voting with its feet
    assertFalse(ChangeRules.adoptionDeclared(45.0, 0));
  }
}
