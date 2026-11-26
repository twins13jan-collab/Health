package eg.gov.health.platform.outpatientdeep;
import java.util.*;
/** Outpatient rules (module 83). A clinic visit closes only
 *  when the full cycle is real: identity verified, eligibility
 *  verified, the treatment plan documented, and a named owner
 *  for follow-up — a visit without a follow-up owner is a
 *  conversation, not care. A no-show is recorded and a
 *  reschedule offered; a patient silently dropped from a list
 *  is a patient lost by the system, not by choice. */
public final class OutpatientRules {
  private OutpatientRules() {}
  public static List<String> closureBlockers(boolean identity, boolean eligibility, boolean plan, String followupOwner) {
    List<String> b = new ArrayList<>();
    if (!identity) b.add("identity_unverified");
    if (!eligibility) b.add("eligibility_unverified");
    if (!plan) b.add("plan_undocumented");
    if (followupOwner == null || followupOwner.isBlank()) b.add("followup_owner_missing");
    return b;
  }
}
