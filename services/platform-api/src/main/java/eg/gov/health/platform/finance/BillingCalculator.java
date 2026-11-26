package eg.gov.health.platform.finance;
import java.math.BigDecimal; import java.math.RoundingMode; import java.util.List;
/** Invoice mathematics (cycle 228): totals are computed server-side only,
 *  coverage splits patient/insurer shares, and a claim can only be priced
 *  from verified line items — never from a client-supplied total. */
public final class BillingCalculator {
  private BillingCalculator() {}
  public record Line(BigDecimal unitPrice, int quantity, BigDecimal coveragePct) {}
  public static BigDecimal lineTotal(Line l) { return l.unitPrice().multiply(BigDecimal.valueOf(l.quantity())).setScale(2, RoundingMode.HALF_UP); }
  public static BigDecimal grandTotal(List<Line> lines) { return lines.stream().map(BillingCalculator::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
  public static BigDecimal insurerShare(List<Line> lines) {
    return lines.stream().map(l -> lineTotal(l).multiply(l.coveragePct()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP))
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
  public static BigDecimal patientShare(List<Line> lines) { return grandTotal(lines).subtract(insurerShare(lines)); }
}
