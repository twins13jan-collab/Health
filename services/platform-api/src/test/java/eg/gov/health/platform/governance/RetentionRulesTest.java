package eg.gov.health.platform.governance;
import org.junit.jupiter.api.Test; import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class RetentionRulesTest {
  @Test void retentionPeriods() {
    assertEquals(25, RetentionRules.retentionYears("discharge_summary"));
    assertEquals(15, RetentionRules.retentionYears("lab_report"));
    assertEquals(100, RetentionRules.retentionYears("death_certificate"));
    assertEquals(10, RetentionRules.retentionYears("other"));
  }
  @Test void disposalGating() {
    var docDate = LocalDate.of(2020, 1, 1);
    assertFalse(RetentionRules.disposalAllowed(docDate, "lab_report", LocalDate.of(2030, 1, 1), true));
    assertTrue(RetentionRules.disposalAllowed(docDate, "lab_report", LocalDate.of(2036, 1, 1), true));
    assertFalse(RetentionRules.disposalAllowed(docDate, "lab_report", LocalDate.of(2036, 1, 1), false));
  }
}
