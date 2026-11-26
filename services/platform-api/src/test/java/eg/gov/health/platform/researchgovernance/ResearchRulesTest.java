package eg.gov.health.platform.researchgovernance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ResearchRulesTest {
  @Test void allFivePillarsPlusIdentifiedException() {
    assertTrue(ResearchRules.approvalBlockers("دراسة سكري", true, true, "قانون حماية البيانات", true, "deidentified", false).isEmpty());
    assertTrue(ResearchRules.approvalBlockers("دراسة", true, true, "أساس", false, "aggregate", false).contains("ethics_unapproved"));
    assertTrue(ResearchRules.approvalBlockers("دراسة", true, true, "أساس", true, "identified", false).contains("identified_data_unjustified"));
    assertTrue(ResearchRules.approvalBlockers("دراسة", true, true, "أساس", true, "identified", true).isEmpty());
    assertTrue(ResearchRules.approvalBlockers("دراسة", false, true, "أساس", true, "aggregate", false).contains("necessity_unjustified"));
  }
}
