package eg.gov.health.platform.clinical;
import java.time.LocalDate; import java.util.*;
/** Simplified Egyptian EPI schedule checkpoints (module 96/102): the due
 *  logic is age-based and deterministic; the full national schedule table is
 *  data (vaccination_schedule table) — this class holds the core milestones
 *  used for due/overdue computation and catch-up flagging. */
public final class VaccinationSchedule {
  private VaccinationSchedule() {}
  public record Milestone(String vaccine, int dueAgeDays) {}
  public static final List<Milestone> MILESTONES = List.of(
    new Milestone("BCG", 0), new Milestone("OPV0", 0), new Milestone("HBV1", 0),
    new Milestone("PENTA1", 60), new Milestone("PENTA2", 120), new Milestone("PENTA3", 180),
    new Milestone("MMR1", 365), new Milestone("MMR2", 545));
  public static List<Milestone> dueMilestones(LocalDate birthDate, LocalDate onDate, Set<String> givenVaccines) {
    long age = java.time.temporal.ChronoUnit.DAYS.between(birthDate, onDate);
    return MILESTONES.stream().filter(m -> m.dueAgeDays() <= age && !givenVaccines.contains(m.vaccine())).toList();
  }
}
