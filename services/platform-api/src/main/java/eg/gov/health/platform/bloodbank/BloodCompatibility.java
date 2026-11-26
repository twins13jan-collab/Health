package eg.gov.health.platform.bloodbank;
import java.util.*;
/** ABO/Rh red-cell compatibility matrix (cycle 223). O- is the universal
 *  red-cell donor; AB+ the universal recipient. Plasma compatibility is the
 *  inverse and handled by the plasma-specific table in the full module. */
public final class BloodCompatibility {
  private BloodCompatibility() {}
  public static final List<String> GROUPS = List.of("O-","O+","A-","A+","B-","B+","AB-","AB+");
  static boolean aboCanDonate(String donor, String recipient) {
    String d = donor.substring(0, donor.length()-1), r = recipient.substring(0, recipient.length()-1);
    return switch (d) {
      case "O" -> true;
      case "A" -> r.equals("A") || r.equals("AB");
      case "B" -> r.equals("B") || r.equals("AB");
      case "AB" -> r.equals("AB");
      default -> false;
    };
  }
  public static boolean canDonateRbc(String donorGroup, String recipientGroup) {
    if (!GROUPS.contains(donorGroup) || !GROUPS.contains(recipientGroup)) return false;
    boolean rhOk = donorGroup.endsWith("-") || recipientGroup.endsWith("+");
    return rhOk && aboCanDonate(donorGroup, recipientGroup);
  }
  public static List<String> compatibleDonorsFor(String recipientGroup) {
    return GROUPS.stream().filter(d -> canDonateRbc(d, recipientGroup)).toList();
  }
}
