package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test; import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class WhoChecklistTest {
  @Test void stageNeedsEveryItem() {
    assertFalse(WhoChecklist.stageComplete("sign_in", Set.of("identity_confirmed","site_marked")));
    assertTrue(WhoChecklist.stageComplete("sign_in",
      Set.of("identity_confirmed","site_marked","consent_confirmed","anaesthesia_check","allergy_checked","airway_assessed")));
  }
  @Test void cannotAdvanceIncomplete() { assertTrue(WhoChecklist.nextStage("sign_in", Set.of()).isEmpty()); }
  @Test void advanceAfterCompletion() {
    var all = Set.copyOf(WhoChecklist.STAGES.get("sign_in"));
    assertEquals("time_out", WhoChecklist.nextStage("sign_in", all).orElseThrow());
  }
}
