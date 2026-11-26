package eg.gov.health.platform.clinicalforms;
/** Structured-forms rules (module 138). Clinicians write on
 *  the ACTIVE version of a template — never a draft, never a
 *  retired one. A form is filed as final only when signed by
 *  its author, and once signed it is immutable: a correction is
 *  a new instance that supersedes the old, so the record shows
 *  both what was written and what replaced it. Filed forms
 *  become structured data, not scanned paper. */
public final class FormRules {
  private FormRules() {}
  public static boolean instantiable(String templateStatus) { return "active".equals(templateStatus); }
  public static boolean filable(boolean signed) { return signed; }
}
