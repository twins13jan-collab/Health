package eg.gov.health.platform.identity;
import eg.gov.health.platform.facility.Facility;
/** What an authority_admin oversees, by remit: the Ministry of
 *  Health follows its own facilities and the private sector it
 *  licenses; the drug regulatory authority follows pharmacies
 *  wherever they are; the health insurance organisation follows
 *  insurance-owned facilities; higher education follows university
 *  and teaching facilities. Pure logic, shared by the oversight
 *  board and the scoped-login check. */
public final class AuthorityScope {
  private AuthorityScope() {}
  public static boolean covers(String authority, Facility f) {
    if (authority == null || f == null) return false;
    return switch (authority) {
      case "moh" -> "moh".equals(f.getOwnership()) || "private".equals(f.getOwnership());
      case "drug" -> "pharmacy".equals(f.getType());
      case "insurance" -> "insurance".equals(f.getOwnership());
      case "higher_education" -> "university".equals(f.getOwnership());
      default -> false;
    };
  }
}
