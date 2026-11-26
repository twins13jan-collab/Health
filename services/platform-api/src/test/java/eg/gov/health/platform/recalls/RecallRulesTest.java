package eg.gov.health.platform.recalls;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class RecallRulesTest {
  @Test void dispensableGate() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(RecallRules.dispensable("released", LocalDate.of(2027, 1, 1), today));
    assertFalse(RecallRules.dispensable("released", LocalDate.of(2026, 1, 1), today)); // expired
    assertFalse(RecallRules.dispensable("quarantined", LocalDate.of(2027, 1, 1), today));
    assertFalse(RecallRules.dispensable("recalled", LocalDate.of(2027, 1, 1), today));
  }
  @Test void fefoExceptionNeedsReason() {
    assertTrue(RecallRules.fefoException(LocalDate.of(2027, 6, 1), LocalDate.of(2027, 1, 1)));
    assertFalse(RecallRules.fefoException(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 1)));
    assertFalse(RecallRules.exceptionDocumented(true, " "));
    assertTrue(RecallRules.exceptionDocumented(true, "الأقرب محجوز لجناح العزل"));
    assertTrue(RecallRules.exceptionDocumented(false, null));
  }
  @Test void adjustmentNeedsApproval() {
    assertFalse(RecallRules.movementAllowed("adjustment", null));
    assertTrue(RecallRules.movementAllowed("adjustment", "مسؤول الجودة"));
    assertTrue(RecallRules.movementAllowed("receive", null));
  }
  @Test void recallClosesOnlyOnFullDisposition() {
    assertFalse(RecallRules.recallClosable(100, 60));
    assertTrue(RecallRules.recallClosable(100, 100));
    assertTrue(RecallRules.recallClosable(0, 0));
  }
}
