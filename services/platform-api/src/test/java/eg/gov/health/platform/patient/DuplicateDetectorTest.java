package eg.gov.health.platform.patient;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class DuplicateDetectorTest {
  @Test void nameAloneNeverRaisesCandidate() {
    var c = DuplicateDetector.evaluate("Ahmed Ali", LocalDate.of(1990,1,1), null, "Ahmed Ali", LocalDate.of(1985,5,5), null);
    assertFalse(DuplicateDetector.isReviewCandidate(c));
  }
  @Test void namePlusBirthDateRaisesExplainableCandidate() {
    var c = DuplicateDetector.evaluate("Ahmed Ali", LocalDate.of(1990,1,1), null, "Ahmed Ali", LocalDate.of(1990,1,1), null);
    assertTrue(DuplicateDetector.isReviewCandidate(c));
    assertTrue(c.reason().contains("same_name") && c.reason().contains("same_birth_date"));
  }
}
