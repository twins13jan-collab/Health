package eg.gov.health.platform.facilityenvironment;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class WasteRulesTest {
  @Test void regulatedWasteNeedsAValidLicenseOnCollectionDay() {
    var until = LocalDate.of(2026, 12, 31); var day = LocalDate.of(2026, 11, 22);
    assertTrue(WasteRules.contractorLicensed("infectious", "LIC-9", until, day));
    assertFalse(WasteRules.contractorLicensed("sharps", "LIC-9", LocalDate.of(2026, 1, 1), day)); // expired
    assertFalse(WasteRules.contractorLicensed("chemical", null, until, day)); // unlicensed
    assertTrue(WasteRules.contractorLicensed("general", null, null, day)); // general stream exempt
  }
  @Test void chainAndStorageBreach() {
    assertTrue(WasteRules.chainComplete("a", "b", "c"));
    assertFalse(WasteRules.chainComplete("a", null, "c"));
    var gen = OffsetDateTime.parse("2026-11-20T08:00+02:00");
    assertTrue(WasteRules.storageBreached("infectious", gen, OffsetDateTime.parse("2026-11-22T10:00+02:00"))); // 50h
    assertFalse(WasteRules.storageBreached("infectious", gen, OffsetDateTime.parse("2026-11-21T08:00+02:00"))); // 24h
    assertFalse(WasteRules.storageBreached("general", gen, OffsetDateTime.parse("2026-11-25T08:00+02:00")));
  }
  @Test void spillClosesOnlyContainedAndDecontaminated() {
    assertFalse(WasteRules.spillClosable(null, true));
    assertFalse(WasteRules.spillClosable(OffsetDateTime.now(), false));
    assertTrue(WasteRules.spillClosable(OffsetDateTime.now(), true));
  }
}
