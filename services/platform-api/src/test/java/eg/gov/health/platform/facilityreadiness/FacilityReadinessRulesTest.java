package eg.gov.health.platform.facilityreadiness;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FacilityReadinessRulesTest {
  @Test void eightChecksAndDisciplinedVerdicts() {
    assertTrue(FacilityReadinessRules.blockers(true, true, true, true, true, true, true, true).isEmpty());
    assertTrue(FacilityReadinessRules.blockers(true, true, false, true, true, true, true, true).contains("equipment"));
    assertTrue(FacilityReadinessRules.activatable(java.util.List.of(), "مدير المنشأة"));
    assertFalse(FacilityReadinessRules.activatable(java.util.List.of(), " "));
    assertFalse(FacilityReadinessRules.activatable(java.util.List.of("stock"), "مدير المنشأة"));
    assertTrue(FacilityReadinessRules.notReadyRecordable(java.util.List.of("stock"), "توريد خلال أسبوعين"));
    assertFalse(FacilityReadinessRules.notReadyRecordable(java.util.List.of("stock"), ""));
  }
}
