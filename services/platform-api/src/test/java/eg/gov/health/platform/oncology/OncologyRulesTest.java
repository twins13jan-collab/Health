package eg.gov.health.platform.oncology;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal; import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class OncologyRulesTest {
  @Test void protocolMustBeMedicallyApprovedAndInDate() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(OncologyRules.protocolUsable(true, LocalDate.of(2027, 1, 1), "active", today));
    assertFalse(OncologyRules.protocolUsable(false, LocalDate.of(2027, 1, 1), "active", today)); // present != approved
    assertFalse(OncologyRules.protocolUsable(true, LocalDate.of(2026, 1, 1), "active", today)); // expired
    assertFalse(OncologyRules.protocolUsable(true, LocalDate.of(2027, 1, 1), "withdrawn", today));
  }
  @Test void verificationSuspendsOutOfConditions() {
    assertTrue(OncologyRules.verificationSuspended(new BigDecimal("7.5"), new BigDecimal("5"), true)); // weight swing
    assertTrue(OncologyRules.verificationSuspended(new BigDecimal("1"), new BigDecimal("5"), false)); // labs out
    assertFalse(OncologyRules.verificationSuspended(new BigDecimal("2"), new BigDecimal("5"), true));
  }
  @Test void independenceAndVarianceDocumentation() {
    assertFalse(OncologyRules.independentVerifier("onc@x", "onc@x"));
    assertTrue(OncologyRules.independentVerifier("onc@x", "nurse@x"));
    assertTrue(OncologyRules.varianceDocumented(BigDecimal.ZERO, null, null));
    assertFalse(OncologyRules.varianceDocumented(new BigDecimal("-50"), null, "onc@x"));
    assertTrue(OncologyRules.varianceDocumented(new BigDecimal("-50"), "تعديل حسب التحمل", "onc@x"));
  }
  @Test void recalledOrUnavailableBlocksDispensing() {
    assertTrue(OncologyRules.dispensable(true, false, null, null));
    assertFalse(OncologyRules.dispensable(true, true, null, null)); // recalled
    assertFalse(OncologyRules.dispensable(false, false, null, null)); // unavailable
    assertTrue(OncologyRules.dispensable(false, false, "بديل مكافئ", "onc@x")); // decided substitute
  }
  @Test void technicalUsersNeverTouchProtocolsOrDoses() {
    assertFalse(OncologyRules.mayModifyProtocol("it_support"));
    assertFalse(OncologyRules.mayHandleDose("it_support"));
    assertTrue(OncologyRules.mayHandleDose("oncology_pharmacist"));
  }
}
