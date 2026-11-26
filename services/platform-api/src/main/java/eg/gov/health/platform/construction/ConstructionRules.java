package eg.gov.health.platform.construction;
import java.util.*;
/** Facility construction & handover rules (module 172). A health
 *  facility is not a building with beds: construction starts only
 *  on a documented need study against the health map, with the
 *  medical design AND the infection-control design approved —
 *  infection control reviews drawings, not finished walls.
 *  Contract changes apply only when approved. And handover is
 *  accepted only when every critical system (medical gas, power,
 *  HVAC, water, network) has PASSED commissioning and the assets,
 *  documents and licences are received — a failed gas test is not
 *  a snag to note, it is a hospital that does not open. */
public final class ConstructionRules {
  private ConstructionRules() {}
  public static final List<String> CRITICAL_SYSTEMS = List.of("medical_gas","power","hvac","water","it_network");
  public static boolean constructionStartPermitted(String needStudyRef, boolean medicalDesignApproved, boolean ipcDesignApproved) {
    return needStudyRef != null && !needStudyRef.isBlank() && medicalDesignApproved && ipcDesignApproved;
  }
  public static boolean handoverPermitted(Map<String,String> latestSystemResults, boolean assets, boolean documents, boolean licenses) {
    if (!assets || !documents || !licenses) return false;
    for (String sys : CRITICAL_SYSTEMS) {
      if (!"pass".equals(latestSystemResults.get(sys))) return false;
    }
    return true;
  }
}
