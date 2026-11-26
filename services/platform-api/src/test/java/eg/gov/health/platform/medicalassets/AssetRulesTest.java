package eg.gov.health.platform.medicalassets;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class AssetRulesTest {
  @Test void calibrationCurrency() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(AssetRules.calibrationCurrent(LocalDate.of(2026, 12, 1), today));
    assertFalse(AssetRules.calibrationCurrent(LocalDate.of(2026, 11, 1), today)); // overdue
    assertFalse(AssetRules.calibrationCurrent(null, today));
  }
  @Test void usabilityGate() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(AssetRules.usable("in_service", LocalDate.of(2027, 1, 1), today));
    assertFalse(AssetRules.usable("in_service", LocalDate.of(2026, 1, 1), today)); // out of calibration -> unfit
    assertFalse(AssetRules.usable("out_of_service", LocalDate.of(2027, 1, 1), today));
    assertFalse(AssetRules.usable("quarantined", LocalDate.of(2027, 1, 1), today));
  }
  @Test void releaseRolesAndSafetyLink() {
    assertTrue(AssetRules.mayRelease("biomedical_engineer"));
    assertTrue(AssetRules.mayRelease("quality_officer"));
    assertFalse(AssetRules.mayRelease("nurse")); // clinician reports, never releases
    assertFalse(AssetRules.safetyCaseRequired(true, null));
    assertTrue(AssetRules.safetyCaseRequired(true, "SAFE-2210"));
    assertTrue(AssetRules.safetyCaseRequired(false, null));
  }
}
