package eg.gov.health.platform.fhir;
import eg.gov.health.platform.patient.Patient;
import org.junit.jupiter.api.Test;
import java.time.LocalDate; import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class FhirMappingTest {
  @Test void patientMapsToFhirR4() {
    var p = new Patient(null, "القاهرة", "29801011234567", "مريض اختبار", LocalDate.of(1998,1,1), "male", null, null, "verified");
    var f = FhirController.toFhirPatient(p);
    assertEquals("Patient", f.get("resourceType"));
    assertEquals("1998-01-01", f.get("birthDate"));
  }
}
