package eg.gov.health.platform.clinicalcomms;
import java.time.*;
/** Clinical-communication rules (module 161). A critical message
 *  is communicated only when the loop closes: the receiver
 *  acknowledges it AND reads it back — "sent" is a hope, not a
 *  handover. A critical alert that stays unacknowledged beyond
 *  the threshold escalates to the next responsible clinician; it
 *  never just ages quietly in an inbox. When the system channel
 *  fails, the fallback channel (phone, in person) is used and
 *  recorded as used. */
public final class CommsRules {
  private CommsRules() {}
  public static final long ACK_ESCALATION_MINUTES = 15;
  public static boolean loopClosed(boolean acknowledged, boolean readBackConfirmed) {
    return acknowledged && readBackConfirmed;
  }
  public static boolean escalationDue(OffsetDateTime sentAt, boolean acknowledged, OffsetDateTime now) {
    return sentAt != null && now != null && !acknowledged
      && Duration.between(sentAt, now).toMinutes() >= ACK_ESCALATION_MINUTES;
  }
}
