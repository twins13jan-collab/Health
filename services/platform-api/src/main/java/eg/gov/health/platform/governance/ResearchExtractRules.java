package eg.gov.health.platform.governance;
import java.util.*;
/** Research/secondary-use extract governance (module 65 — research governance): a research extract
 *  may NEVER contain direct identifiers, regardless of who asks. Quasi-
 *  identifiers are only released in generalised form. Approval requires an
 *  ethics/governance decision recorded BEFORE any extract is produced. */
public final class ResearchExtractRules {
  private ResearchExtractRules() {}
  public static final Set<String> DIRECT_IDENTIFIERS = Set.of(
    "national_id","full_name","full_name_ar","phone","email","address","medical_record_number","photo_url");
  public static final Set<String> QUASI_IDENTIFIERS = Set.of("birth_date","facility_id","admission_date","discharge_date");
  public static List<String> violations(Collection<String> requestedFields) {
    return requestedFields.stream().filter(DIRECT_IDENTIFIERS::contains).sorted().toList();
  }
  public static boolean extractAllowed(String requestStatus, Collection<String> requestedFields) {
    return "approved".equals(requestStatus) && violations(requestedFields).isEmpty();
  }
}
