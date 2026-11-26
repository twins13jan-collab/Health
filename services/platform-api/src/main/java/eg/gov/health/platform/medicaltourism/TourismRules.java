package eg.gov.health.platform.medicaltourism;
/** Medical-tourism rules (module 75). An international patient
 *  is scheduled when the file is translated, consent is
 *  obtained, and the financial estimate is approved — no
 *  surprises in language, rights, or money. Health data crosses
 *  a border only on a legal basis AND with the patient's
 *  consent; either one alone is not enough. Follow-up with the
 *  home provider happens only as the patient permits. */
public final class TourismRules {
  private TourismRules() {}
  public static boolean schedulable(boolean fileTranslated, boolean consent, boolean estimateApproved) {
    return fileTranslated && consent && estimateApproved;
  }
  public static boolean transferLawful(String legalBasis, boolean patientConsent) {
    return legalBasis != null && !legalBasis.isBlank() && patientConsent;
  }
  public static boolean followupPermitted(boolean patientPermission) { return patientPermission; }
}
