package eg.gov.health.platform.servicedesk;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ServiceDeskRulesTest {
  @Test void specialTracksAreSeparated() {
    assertEquals("security", ServiceDeskRules.routeTrack("security"));
    assertEquals("clinical", ServiceDeskRules.routeTrack("clinical_safety"));
    assertEquals("standard", ServiceDeskRules.routeTrack("printing"));
  }
  @Test void specialTracksResolveOnlyByTheirAuthority() {
    assertFalse(ServiceDeskRules.mayResolve("security", "receptionist"));
    assertTrue(ServiceDeskRules.mayResolve("security", "admin"));
    assertFalse(ServiceDeskRules.mayResolve("clinical", "storekeeper"));
    assertTrue(ServiceDeskRules.mayResolve("standard", "receptionist"));
  }
  @Test void responseSlaIsPerSeverity() {
    var created = OffsetDateTime.parse("2026-11-22T10:00:00+02:00");
    assertTrue(ServiceDeskRules.responseBreached("critical", created, created.plusMinutes(40))); // target 15
    assertFalse(ServiceDeskRules.responseBreached("critical", created, created.plusMinutes(10)));
    assertFalse(ServiceDeskRules.responseBreached("low", created, created.plusMinutes(40)));      // target 480
  }
}
