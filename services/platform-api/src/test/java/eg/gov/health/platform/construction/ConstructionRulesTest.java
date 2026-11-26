package eg.gov.health.platform.construction;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ConstructionRulesTest {
  @Test void constructionStartsOnNeedAndApprovedDesignsOnly() {
    assertTrue(ConstructionRules.constructionStartPermitted("دراسة احتياج 2026", true, true));
    assertFalse(ConstructionRules.constructionStartPermitted(null, true, true));
    assertFalse(ConstructionRules.constructionStartPermitted("دراسة", false, true));
    assertFalse(ConstructionRules.constructionStartPermitted("دراسة", true, false)); // IPC reviews drawings, not finished walls
  }
  @Test void handoverNeedsEveryCriticalSystemPassedAndEverythingReceived() {
    var all = Map.of("medical_gas","pass","power","pass","hvac","pass","water","pass","it_network","pass");
    assertTrue(ConstructionRules.handoverPermitted(all, true, true, true));
    var gasFailed = new HashMap<>(all); gasFailed.put("medical_gas", "fail");
    assertFalse(ConstructionRules.handoverPermitted(gasFailed, true, true, true)); // a failed gas test = a hospital that does not open
    var missing = new HashMap<>(all); missing.remove("water");
    assertFalse(ConstructionRules.handoverPermitted(missing, true, true, true));
    assertFalse(ConstructionRules.handoverPermitted(all, true, true, false)); // licences not received
  }
}
