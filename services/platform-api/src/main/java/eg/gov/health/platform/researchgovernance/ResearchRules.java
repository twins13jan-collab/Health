package eg.gov.health.platform.researchgovernance;
import java.util.*;
/** Research-governance rules (module 65). Secondary use of
 *  health data is approved only when all of purpose, necessity,
 *  data minimization, legal basis and ethics approval hold —
 *  remove any one and the request is a data grab with a study
 *  title. Identified data is the exception, granted only when
 *  de-identified or aggregate data demonstrably cannot answer
 *  the question; and re-identification is prohibited and
 *  export monitored, because consent given for care was never
 *  consent given for everything. */
public final class ResearchRules {
  private ResearchRules() {}
  public static List<String> approvalBlockers(String purpose, boolean necessity, boolean minimal, String legalBasis,
                                              boolean ethicsApproved, String dataForm, boolean identifiedJustified) {
    List<String> b = new ArrayList<>();
    if (purpose == null || purpose.isBlank()) b.add("purpose_missing");
    if (!necessity) b.add("necessity_unjustified");
    if (!minimal) b.add("dataset_not_minimal");
    if (legalBasis == null || legalBasis.isBlank()) b.add("legal_basis_missing");
    if (!ethicsApproved) b.add("ethics_unapproved");
    if ("identified".equals(dataForm) && !identifiedJustified) b.add("identified_data_unjustified");
    return b;
  }
}
