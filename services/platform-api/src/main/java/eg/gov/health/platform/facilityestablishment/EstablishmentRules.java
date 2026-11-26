package eg.gov.health.platform.facilityestablishment;
import java.util.*;
/** Establishment rules (module 73). A new facility opens when
 *  every gate has honestly passed: the population-need study,
 *  the site, the infection-control-approved design, every
 *  commissioning check — equipment installed AND tested, IT
 *  systems, staff trained, a real trial run — and the opening
 *  licence itself. A ribbon is not a gate. */
public final class EstablishmentRules {
  private EstablishmentRules() {}
  public static final List<String> REQUIRED_CHECKS = List.of("equipment_installation","equipment_testing","it_systems","staff_training","trial_run");
  public static List<String> missingGates(boolean needsStudy, boolean site, boolean ipcDesign,
                                          Map<String,Boolean> checks, boolean licenceGranted) {
    List<String> missing = new ArrayList<>();
    if (!needsStudy) missing.add("needs_study");
    if (!site) missing.add("site_selection");
    if (!ipcDesign) missing.add("infection_control_design");
    for (String c : REQUIRED_CHECKS) if (!Boolean.TRUE.equals(checks.get(c))) missing.add("commissioning:" + c);
    if (!licenceGranted) missing.add("opening_licence");
    return missing;
  }
}
