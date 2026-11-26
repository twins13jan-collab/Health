package eg.gov.health.platform.identity;

import java.util.ArrayList;
import java.util.List;

/** Production password policy for locally managed accounts.
 *  Pure logic so it is unit-testable without a database. */
public final class PasswordPolicy {
  public static final int MIN_LENGTH = 10;
  private PasswordPolicy() {}

  /** Returns human-readable violations (Arabic); empty list means acceptable. */
  public static List<String> violations(String password) {
    return violations(password, MIN_LENGTH);
  }

  /** Settings-driven variant (security.password_min_length). */
  public static List<String> violations(String password, int minLength) {
    List<String> v = new ArrayList<>();
    if (password == null || password.length() < minLength)
      v.add("كلمة المرور يجب أن تكون " + minLength + " أحرف على الأقل");
    if (password != null) {
      boolean letter = password.chars().anyMatch(Character::isLetter);
      boolean digit = password.chars().anyMatch(Character::isDigit);
      if (!letter) v.add("كلمة المرور يجب أن تحتوي على حرف واحد على الأقل");
      if (!digit) v.add("كلمة المرور يجب أن تحتوي على رقم واحد على الأقل");
    }
    return v;
  }

  public static boolean acceptable(String password) {
    return violations(password).isEmpty();
  }
}
