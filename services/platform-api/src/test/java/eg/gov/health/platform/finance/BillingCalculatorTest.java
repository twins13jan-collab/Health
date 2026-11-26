package eg.gov.health.platform.finance;
import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class BillingCalculatorTest {
  @Test void totalsAndSplit() {
    var lines = List.of(
      new BillingCalculator.Line(new BigDecimal("100.00"), 2, new BigDecimal("80")),
      new BillingCalculator.Line(new BigDecimal("50.00"), 1, BigDecimal.ZERO));
    assertEquals(new BigDecimal("250.00"), BillingCalculator.grandTotal(lines));
    assertEquals(new BigDecimal("160.00"), BillingCalculator.insurerShare(lines));
    assertEquals(new BigDecimal("90.00"), BillingCalculator.patientShare(lines));
  }
  @Test void emptyInvoiceIsZero() { assertEquals(BigDecimal.ZERO, BillingCalculator.grandTotal(List.of())); }
}
