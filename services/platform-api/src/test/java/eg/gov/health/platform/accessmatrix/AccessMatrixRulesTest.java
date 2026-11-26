package eg.gov.health.platform.accessmatrix;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AccessMatrixRulesTest {
  @Test void longestPrefixAdminAndOpenDefault() {
    var rules = List.of(
      new AccessMatrixRules.Rule("/api/pharmacy", Set.of("pharmacist")),
      new AccessMatrixRules.Rule("/api/billing", Set.of("finance_manager")));
    assertTrue(AccessMatrixRules.allowed(rules, "/api/pharmacy/prescriptions", "pharmacist"));
    assertFalse(AccessMatrixRules.allowed(rules, "/api/pharmacy/prescriptions", "receptionist"));
    assertTrue(AccessMatrixRules.allowed(rules, "/api/billing/invoices", "admin"));
    assertTrue(AccessMatrixRules.allowed(rules, "/api/journeys", "receptionist"));
    assertFalse(AccessMatrixRules.allowed(rules, "/api/billing/invoices", null));
  }
  @Test void writesDefaultClosedReadsGovernedByDomain() {
    var rules = List.of(
      new AccessMatrixRules.Rule("/api/pharmacy", Set.of("pharmacist")),
      new AccessMatrixRules.Rule("/api/billing", Set.of("finance_manager")));
    // writes: governed domains check the role set; ungoverned refuse non-admins
    assertTrue(AccessMatrixRules.writeAllowed(rules, "/api/pharmacy/prescriptions", "pharmacist"));
    assertFalse(AccessMatrixRules.writeAllowed(rules, "/api/pharmacy/prescriptions", "finance_manager"));
    assertFalse(AccessMatrixRules.writeAllowed(rules, "/api/journeys", "receptionist"));
    assertTrue(AccessMatrixRules.writeAllowed(rules, "/api/journeys", "admin"));
    assertTrue(AccessMatrixRules.writeAllowed(rules, "/api/journeys", "super_admin"));
    assertFalse(AccessMatrixRules.writeAllowed(rules, "/api/journeys", "national_admin"));
    assertFalse(AccessMatrixRules.writeAllowed(rules, "/api/journeys", "governorate_admin"));
    // reads: governed domains limited to their set; oversight tiers read across
    assertTrue(AccessMatrixRules.readAllowed(rules, "/api/billing/invoices", "finance_manager"));
    assertFalse(AccessMatrixRules.readAllowed(rules, "/api/billing/invoices", "receptionist"));
    assertTrue(AccessMatrixRules.readAllowed(rules, "/api/billing/invoices", "governorate_admin"));
    assertTrue(AccessMatrixRules.readAllowed(rules, "/api/billing/invoices", "directorate_admin"));
    assertTrue(AccessMatrixRules.readAllowed(rules, "/api/journeys", "receptionist"));
    assertFalse(AccessMatrixRules.readAllowed(rules, "/api/billing/invoices", "patient"));
  }
}
