package eg.gov.health.platform.ledger;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class LedgerRulesTest {
  private BigDecimal[] l(String d, String c) { return new BigDecimal[]{ new BigDecimal(d), new BigDecimal(c) }; }
  @Test void balanceRule() {
    assertTrue(LedgerRules.balanced(List.<BigDecimal[]>of(l("100","0"), l("0","100"))));
    assertFalse(LedgerRules.balanced(List.<BigDecimal[]>of(l("100","0"), l("0","90")))); // unbalanced refused
    assertFalse(LedgerRules.balanced(List.of()));
    assertFalse(LedgerRules.balanced(List.<BigDecimal[]>of(l("0","0"))));
  }
  @Test void rolesAndPeriods() {
    assertTrue(LedgerRules.mayPost("finance_manager"));
    assertFalse(LedgerRules.mayPost("receptionist"));
    assertFalse(LedgerRules.mayPost("accountant")); // accountant reviews; finance manager posts
    assertTrue(LedgerRules.periodPostable("open"));
    assertTrue(LedgerRules.periodPostable("reopened"));
    assertFalse(LedgerRules.periodPostable("closed"));
  }
  @Test void bankMatchingNeedsReferenceAndAmount() {
    assertTrue(LedgerRules.matchable("REF-1", "REF-1", new BigDecimal("500"), new BigDecimal("500")));
    assertFalse(LedgerRules.matchable(null, "REF-1", new BigDecimal("500"), new BigDecimal("500"))); // no reference stays unmatched
    assertFalse(LedgerRules.matchable("REF-1", "REF-2", new BigDecimal("500"), new BigDecimal("500")));
    assertFalse(LedgerRules.matchable("REF-1", "REF-1", new BigDecimal("500"), new BigDecimal("501")));
  }
}
