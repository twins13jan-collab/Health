package eg.gov.health.platform.clinical;
import java.util.*;
/** Allergy safety rules: drug-class membership and documented cross-
 *  reactivity. A prescription matching an active allergy (same drug, same
 *  class, or a cross-reactive class) is a HARD STOP requiring override by a
 *  senior clinician with documented reason — never a silent warning. */
public final class AllergyRules {
  private AllergyRules() {}
  public static final Map<String, Set<String>> DRUG_CLASS = Map.ofEntries(
    Map.entry("AMOXICILLIN", Set.of("penicillins")), Map.entry("AMPICILLIN", Set.of("penicillins")),
    Map.entry("PENICILLIN_G", Set.of("penicillins")),
    Map.entry("CEFTRIAXONE", Set.of("cephalosporins")), Map.entry("CEFUROXIME", Set.of("cephalosporins")),
    Map.entry("CEPHALEXIN", Set.of("cephalosporins")),
    Map.entry("SULFAMETHOXAZOLE", Set.of("sulfonamides")), Map.entry("ASPIRIN", Set.of("nsaids")),
    Map.entry("IBUPROFEN", Set.of("nsaids")), Map.entry("DICLOFENAC", Set.of("nsaids")));
  public static final Map<String, Set<String>> CROSS_REACTIVE = Map.of(
    "penicillins", Set.of("cephalosporins"),
    "cephalosporins", Set.of("penicillins"));
  public record AllergyView(String allergen, String allergenClass, String severity, String status) {}
  public static Optional<String> conflict(String drugCode, Collection<AllergyView> activeAllergies) {
    if (drugCode == null) return Optional.empty();
    String drug = drugCode.toUpperCase();
    Set<String> drugClasses = DRUG_CLASS.getOrDefault(drug, Set.of());
    for (var a : activeAllergies) {
      if (!"active".equals(a.status())) continue;
      if (a.allergen() != null && a.allergen().equalsIgnoreCase(drug))
        return Optional.of("نفس الدواء: " + drug + " (خطورة " + a.severity() + ")");
      if (a.allergenClass() != null) {
        if (drugClasses.contains(a.allergenClass()))
          return Optional.of("نفس الفئة الدوائية: " + a.allergenClass());
        var cross = CROSS_REACTIVE.getOrDefault(a.allergenClass(), Set.of());
        for (String dc : drugClasses) if (cross.contains(dc))
          return Optional.of("تفاعل تصالبي موثق: حساسية " + a.allergenClass() + " مع فئة " + dc);
      }
    }
    return Optional.empty();
  }
}
