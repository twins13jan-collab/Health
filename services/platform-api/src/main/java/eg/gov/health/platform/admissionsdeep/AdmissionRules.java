package eg.gov.health.platform.admissionsdeep;
import java.util.*;
/** Admission & discharge rules (module 84). A transfer
 *  admission needs the receiving side's acceptance — a patient
 *  is not shipped on hope. A discharge summary finalizes only
 *  complete: diagnoses, procedures, instructions, warnings,
 *  medication plan and follow-up plan — a summary missing its
 *  warnings sends the patient home unarmed. Discharge against
 *  medical advice follows the same law as the ER: risks
 *  explained, departure signed. */
public final class AdmissionRules {
  private AdmissionRules() {}
  public static boolean admittable(String justification, boolean transfer, boolean receivingAcceptance) {
    if (justification == null || justification.isBlank()) return false;
    return !transfer || receivingAcceptance;
  }
  public static List<String> summaryGaps(String diagnoses, String procedures, String instructions,
                                         String warnings, String medicationsPlan, String followupPlan) {
    List<String> gaps = new ArrayList<>();
    if (blank(diagnoses)) gaps.add("diagnoses");
    if (blank(procedures)) gaps.add("procedures");
    if (blank(instructions)) gaps.add("instructions");
    if (blank(warnings)) gaps.add("warnings");
    if (blank(medicationsPlan)) gaps.add("medications_plan");
    if (blank(followupPlan)) gaps.add("followup_plan");
    return gaps;
  }
  private static boolean blank(String s) { return s == null || s.isBlank(); }
  public static boolean amaDocumented(boolean risksExplained, boolean signed) { return risksExplained && signed; }
}
