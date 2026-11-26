package eg.gov.health.platform.patient;
import java.time.LocalDate;
/** MPI duplicate rules (Catalog cycle 217): NEVER auto-merge on name alone.
 *  A candidate is raised only with explainable evidence; a human identity
 *  officer decides. National-id equality is a hard duplicate (rejected at
 *  registration), name+birthdate or phone+birthdate raise a review case. */
public final class DuplicateDetector {
  private DuplicateDetector() {}
  public record Candidate(String reason, int score) {}
  public static Candidate evaluate(String nameA, LocalDate dobA, String phoneA, String nameB, LocalDate dobB, String phoneB) {
    int score = 0; var reasons = new java.util.ArrayList<String>();
    if (nameA != null && nameA.trim().equalsIgnoreCase(nameB == null ? "" : nameB.trim())) { score += 40; reasons.add("same_name"); }
    if (dobA != null && dobA.equals(dobB)) { score += 40; reasons.add("same_birth_date"); }
    if (phoneA != null && !phoneA.isBlank() && phoneA.equals(phoneB)) { score += 30; reasons.add("same_phone"); }
    return new Candidate(String.join("+", reasons), score);
  }
  public static boolean isReviewCandidate(Candidate c) { return c.score() >= 70; }
}
