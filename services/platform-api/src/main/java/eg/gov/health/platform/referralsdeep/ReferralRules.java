package eg.gov.health.platform.referralsdeep;
import java.time.*;
/** Referral & waiting-list rules (module 110). A referral
 *  travels with its clinical reason and attachments — a naked
 *  name is not a referral. A rejection is a decision only with
 *  a reason AND an alternative; a rejection without an
 *  alternative is a dead end dressed as an answer. Waiting
 *  lists order by a declared fairness rule — clinical priority
 *  first, then waiting time — computed in the open, never by
 *  who called whom. */
public final class ReferralRules {
  private ReferralRules() {}
  public static boolean submittable(String clinicalReason, boolean attachmentsComplete) {
    return clinicalReason != null && !clinicalReason.isBlank() && attachmentsComplete;
  }
  public static boolean rejectionComplete(String reason, String alternative) {
    return reason != null && !reason.isBlank() && alternative != null && !alternative.isBlank();
  }
  public static int compareWaitlist(int priorityA, LocalDate sinceA, int priorityB, LocalDate sinceB) {
    if (priorityA != priorityB) return Integer.compare(priorityB, priorityA); // higher priority first
    return sinceA.compareTo(sinceB); // then longest waiting first
  }
}
