package eg.gov.health.platform.fhir;
import eg.gov.health.platform.clinical.*; import eg.gov.health.platform.common.BizException; import eg.gov.health.platform.patient.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** HL7 FHIR R4 exchange surface (Catalog: FHIR for modern exchange).
 *  Maps the internal domain to FHIR resources; internal ids stay stable so
 *  links survive identity merges (merged patients return the target link). */
@RestController @RequestMapping("/fhir")
public class FhirController {
  private final PatientRepository patients; private final EncounterRepository encounters;
  public FhirController(PatientRepository patients, EncounterRepository encounters) { this.patients = patients; this.encounters = encounters; }
  public static Map<String, Object> toFhirPatient(Patient p) {
    var m = new LinkedHashMap<String, Object>();
    m.put("resourceType", "Patient"); if (p.getId() != null) m.put("id", p.getId().toString()); m.put("active", "active".equals(p.getStatus()));
    if (p.getNationalId() != null) m.put("identifier", List.of(Map.of("system", "https://khattab-ai.com/national-id", "value", p.getNationalId())));
    m.put("name", List.of(Map.of("text", p.getFullName())));
    if (p.getGender() != null) m.put("gender", p.getGender());
    if (p.getBirthDate() != null) m.put("birthDate", p.getBirthDate().toString());
    if (p.getMergedInto() != null) m.put("link", List.of(Map.of("other", Map.of("reference", "Patient/" + p.getMergedInto()), "type", "replaced-by")));
    return m;
  }
  public static Map<String, Object> toFhirEncounter(Encounter e) {
    var m = new LinkedHashMap<String, Object>();
    m.put("resourceType", "Encounter"); if (e.getId() != null) m.put("id", e.getId().toString());
    m.put("status", "closed".equals(e.getStatus()) || "completed".equals(e.getStatus()) ? "finished" : "in-progress");
    m.put("class", Map.of("code", "emergency".equals(e.getType()) ? "EMER" : "AMB"));
    m.put("subject", Map.of("reference", "Patient/" + e.getPatientId()));
    m.put("period", Map.of("start", e.getArrivedAt().toString()));
    if (e.getChiefComplaint() != null) m.put("reasonCode", List.of(Map.of("text", e.getChiefComplaint())));
    return m;
  }
  @GetMapping("/Patient/{id}")
  Map<String, Object> patient(@PathVariable UUID id) {
    return toFhirPatient(patients.findById(id).orElseThrow(() -> BizException.notFound("patient_not_found", "Patient not found")));
  }
  @GetMapping("/Encounter/{id}")
  Map<String, Object> encounter(@PathVariable UUID id) {
    return toFhirEncounter(encounters.findById(id).orElseThrow(() -> BizException.notFound("encounter_not_found", "Encounter not found")));
  }
}
