package eg.gov.health.platform.finance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FinanceBoardRulesTest {
  @Test void agingBuckets() {
    assertEquals("current", FinanceBoardRules.agingBucket(0));
    assertEquals("current", FinanceBoardRules.agingBucket(30));
    assertEquals("d31_60", FinanceBoardRules.agingBucket(45));
    assertEquals("d61_90", FinanceBoardRules.agingBucket(90));
    assertEquals("over_90", FinanceBoardRules.agingBucket(91));
  }
  @Test void rejectedInvoicesTopTheQueue() {
    assertEquals(0, FinanceBoardRules.invoiceRank("rejected", 1));
    assertTrue(FinanceBoardRules.invoiceRank("rejected", 1)
        < FinanceBoardRules.invoiceRank("issued", 200));
  }
  @Test void staleDraftsOutrankYoungReceivables() {
    assertEquals(1, FinanceBoardRules.invoiceRank("draft", 8));
    assertEquals(5, FinanceBoardRules.invoiceRank("draft", 7));
    assertTrue(FinanceBoardRules.invoiceRank("draft", 30)
        < FinanceBoardRules.invoiceRank("issued", 5));
  }
  @Test void receivablesAgeInOrder() {
    assertTrue(FinanceBoardRules.invoiceRank("claimed", 120)
        < FinanceBoardRules.invoiceRank("claimed", 45));
    assertTrue(FinanceBoardRules.invoiceRank("issued", 45)
        < FinanceBoardRules.invoiceRank("claimed", 10));
    assertTrue(FinanceBoardRules.receivable("partially_paid"));
    assertFalse(FinanceBoardRules.receivable("paid"));
  }
  @Test void settledBusinessLeavesTheQueue() {
    assertEquals(9, FinanceBoardRules.invoiceRank("paid", 300));
    assertEquals(9, FinanceBoardRules.invoiceRank("cancelled", 300));
  }
}
