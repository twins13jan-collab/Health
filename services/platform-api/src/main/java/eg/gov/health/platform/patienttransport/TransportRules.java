package eg.gov.health.platform.patienttransport;
import java.time.*;
/** Non-emergency transport rules (module 154). A dialysis or
 *  therapy trip is booked only with a VERIFIED provider whose
 *  licence covers the trip date, and the vehicle must match the
 *  need — wheelchair, stretcher, oxygen. A patient who needs
 *  oxygen never rides with a provider that cannot supply it.
 *  Completion, delay and complaints are recorded per trip, so a
 *  provider that keeps a dialysis patient waiting answers for a
 *  pattern, not an anecdote. */
public final class TransportRules {
  private TransportRules() {}
  public static final int DELAY_REVIEW_MINUTES = 30;
  public static boolean providerUsable(boolean verified, LocalDate licenceValidUntil, LocalDate tripDate,
                                       boolean oxygenNeeded, boolean oxygenCapable,
                                       String needType, boolean stretcherCapable) {
    if (!verified) return false;
    if (licenceValidUntil == null || tripDate == null || licenceValidUntil.isBefore(tripDate)) return false;
    if (oxygenNeeded && !oxygenCapable) return false;
    if ("stretcher".equals(needType) && !stretcherCapable) return false;
    return true;
  }
  public static boolean delayNeedsReview(Integer delayMinutes) {
    return delayMinutes != null && delayMinutes > DELAY_REVIEW_MINUTES;
  }
}
