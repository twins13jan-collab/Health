package eg.gov.health.platform.devicevigilance;
/** Device-vigilance rules (module 168). Every device incident is
 *  reported and risk-assessed; serious harm and death carry a
 *  regulatory-reporting duty, and the case cannot close while a
 *  required regulatory report is still unsent. Field safety
 *  actions — correction, quarantine, recall — take the device
 *  out of service in the tracking record, and the incident stays
 *  linked to its maintenance history, its lot and any training
 *  need it revealed. */
public final class VigilanceRules {
  private VigilanceRules() {}
  public static boolean regulatoryReportRequired(String severity, String incidentType) {
    return "serious".equals(severity) || "death".equals(severity);
  }
  public static boolean caseClosable(boolean regulatoryRequired, boolean regulatoryReported, String actionStatus) {
    if (regulatoryRequired && !regulatoryReported) return false;
    return actionStatus == null || "completed".equals(actionStatus) || "none".equals(actionStatus);
  }
  public static String trackingStatusFor(String actionType) {
    return switch (actionType == null ? "" : actionType) {
      case "recall" -> "recalled";
      case "quarantine" -> "quarantined";
      default -> "in_maintenance";
    };
  }
}
