package eg.gov.health.platform.deathdata;
/** Death-data rules (module 159). A death enters official
 *  statistics only with an approved-code cause certified by
 *  the certifier — mortality statistics built on uncertified
 *  causes mislead every policy built on them. Corrections
 *  append a historical record naming the field, the old and
 *  new values and the reason; a death record is never silently
 *  overwritten, because the correction itself is evidence. */
public final class DeathRules {
  private DeathRules() {}
  public static boolean statisticsReady(String causeCode, boolean causeCertified) {
    return causeCode != null && !causeCode.isBlank() && causeCertified;
  }
  public static boolean correctionValid(String field, String newValue, String reason) {
    return field != null && !field.isBlank() && newValue != null && !newValue.isBlank()
      && reason != null && !reason.isBlank();
  }
}
