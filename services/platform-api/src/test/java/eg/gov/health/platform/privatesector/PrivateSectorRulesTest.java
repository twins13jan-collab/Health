package eg.gov.health.platform.privatesector;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PrivateSectorRulesTest {

  private final LocalDate today = LocalDate.of(2026, 11, 23);

  @Test void licenseStates() {
    assertEquals("not_recorded", PrivateSectorRules.licenseState(null, today));
    assertEquals("expired", PrivateSectorRules.licenseState(LocalDate.of(2026, 11, 22), today));
    assertEquals("expiring_soon", PrivateSectorRules.licenseState(LocalDate.of(2027, 2, 20), today));
    assertEquals("expiring_soon", PrivateSectorRules.licenseState(today, today));
    assertEquals("valid", PrivateSectorRules.licenseState(LocalDate.of(2027, 6, 1), today));
  }

  @Test void warnWindowBoundary() {
    assertEquals(90, PrivateSectorRules.LICENSE_WARN_DAYS);
    assertEquals("expiring_soon", PrivateSectorRules.licenseState(today.plusDays(90), today));
    assertEquals("valid", PrivateSectorRules.licenseState(today.plusDays(91), today));
  }

  @Test void attentionRankOrdersExpiredThenPendingThenRenewals() {
    assertEquals(0, PrivateSectorRules.attentionRank("active", "expired"));
    assertEquals(1, PrivateSectorRules.attentionRank("pending", "valid"));
    assertEquals(2, PrivateSectorRules.attentionRank("active", "expiring_soon"));
    assertEquals(3, PrivateSectorRules.attentionRank("active", "not_recorded"));
    assertEquals(4, PrivateSectorRules.attentionRank("active", "valid"));
    assertTrue(PrivateSectorRules.attentionRank("suspended", "expired") < PrivateSectorRules.attentionRank("pending", "valid"));
  }

  @Test void typeGroupsCoverThePrivateFamilies() {
    assertEquals("hospitals", PrivateSectorRules.typeGroup("hospital"));
    assertEquals("hospitals", PrivateSectorRules.typeGroup("general_hospital"));
    assertEquals("clinics", PrivateSectorRules.typeGroup("clinic"));
    assertEquals("labs", PrivateSectorRules.typeGroup("lab"));
    assertEquals("labs", PrivateSectorRules.typeGroup("laboratory"));
    assertEquals("pharmacies", PrivateSectorRules.typeGroup("pharmacy"));
    assertEquals("centers", PrivateSectorRules.typeGroup("center"));
    assertEquals("centers", PrivateSectorRules.typeGroup("specialized_center"));
    assertEquals("other", PrivateSectorRules.typeGroup("blood_bank"));
    assertEquals("other", PrivateSectorRules.typeGroup(null));
  }

  @Test void dialysisAndOncologyStayOtherUntilTheirOwnFamilyExists() {
    // Standalone dialysis / oncology centres are managed as their own
    // operational systems (Phase 5); in the sector grouping they are
    // deliberately not folded into hospitals.
    assertEquals("other", PrivateSectorRules.typeGroup("dialysis_center"));
    assertEquals("other", PrivateSectorRules.typeGroup("oncology_center"));
  }
}
