package eg.gov.health.platform.cycles;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CycleStateMachineTest {
  @Test void happyPathIsLegal() {
    assertTrue(CycleStateMachine.isLegal("draft", "open"));
    assertTrue(CycleStateMachine.isLegal("open", "in_progress"));
    assertTrue(CycleStateMachine.isLegal("in_progress", "pending_review"));
    assertTrue(CycleStateMachine.isLegal("pending_review", "completed"));
    assertTrue(CycleStateMachine.isLegal("completed", "closed"));
  }
  @Test void skippingReviewGateIsIllegal() {
    assertFalse(CycleStateMachine.isLegal("in_progress", "completed"));
    assertFalse(CycleStateMachine.isLegal("draft", "completed"));
    assertFalse(CycleStateMachine.isLegal("open", "closed"));
  }
  @Test void terminalStatesNeverLeave() {
    for (String to : CycleStateMachine.STATUSES) {
      assertFalse(CycleStateMachine.isLegal("closed", to));
    }
  }
  @Test void exceptionTransitionsRequireReason() {
    assertTrue(CycleStateMachine.REASON_REQUIRED.contains("cancelled"));
    assertTrue(CycleStateMachine.REASON_REQUIRED.contains("corrected"));
    assertTrue(CycleStateMachine.REASON_REQUIRED.contains("reconciled"));
    assertFalse(CycleStateMachine.REASON_REQUIRED.contains("open"));
  }
  @Test void failureReconcilesBeforeClose() {
    assertTrue(CycleStateMachine.isLegal("failed", "reconciled"));
    assertFalse(CycleStateMachine.isLegal("failed", "closed"));
  }
}
