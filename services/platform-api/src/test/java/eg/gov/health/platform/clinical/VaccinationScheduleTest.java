package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test; import java.time.LocalDate; import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class VaccinationScheduleTest {
  @Test void newbornDueBirthDosesOnly() {
    var due = VaccinationSchedule.dueMilestones(LocalDate.of(2026,1,1), LocalDate.of(2026,1,2), Set.of());
    assertEquals(3, due.size());
  }
  @Test void givenDosesExcludedAndLaterOnesAppear() {
    var due = VaccinationSchedule.dueMilestones(LocalDate.of(2025,1,1), LocalDate.of(2025,4,1), Set.of("BCG","OPV0","HBV1"));
    assertTrue(due.stream().anyMatch(m -> m.vaccine().equals("PENTA1")));
    assertFalse(due.stream().anyMatch(m -> m.vaccine().equals("BCG")));
  }
}
