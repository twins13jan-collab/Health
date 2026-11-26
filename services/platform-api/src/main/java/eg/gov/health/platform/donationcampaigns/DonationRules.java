package eg.gov.health.platform.donationcampaigns;
import java.time.*;
/** Donation-campaign rules (module 155). Donor eligibility is
 *  checked on the day — age, weight, haemoglobin and health — and
 *  the donation interval is respected: whole blood again before
 *  90 days is a deferral, however enthusiastic the donor and
 *  however empty the shelves. A reactive screening result belongs
 *  to the donor alone: it is communicated confidentially, with a
 *  referral, and never appears in campaign reporting. Units are
 *  allocated against demand, never beyond what was collected. */
public final class DonationRules {
  private DonationRules() {}
  public static final int MIN_INTERVAL_DAYS = 90;
  public static boolean intervalRespected(LocalDate lastDonation, LocalDate campaignDate) {
    if (lastDonation == null) return true;
    return !lastDonation.plusDays(MIN_INTERVAL_DAYS).isAfter(campaignDate);
  }
  public static boolean donorEligible(Boolean ageOk, Boolean weightOk, Boolean hemoglobinOk, Boolean healthOk, boolean intervalOk) {
    return Boolean.TRUE.equals(ageOk) && Boolean.TRUE.equals(weightOk) && Boolean.TRUE.equals(hemoglobinOk)
      && Boolean.TRUE.equals(healthOk) && intervalOk;
  }
  public static boolean allocationPermitted(int alreadyAllocated, int requested, int collected) {
    return alreadyAllocated + requested <= collected;
  }
}
