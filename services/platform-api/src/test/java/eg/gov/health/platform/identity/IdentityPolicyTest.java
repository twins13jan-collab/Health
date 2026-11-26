package eg.gov.health.platform.identity;

import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;

class IdentityPolicyTest {

  @Test void passwordPolicyRejectsShort() {
    assertFalse(PasswordPolicy.acceptable("Ab1"));
    assertFalse(PasswordPolicy.violations("Ab1").isEmpty());
  }

  @Test void passwordPolicyRequiresLetterAndDigit() {
    assertFalse(PasswordPolicy.acceptable("abcdefghij")); // no digit
    assertFalse(PasswordPolicy.acceptable("1234567890")); // no letter
  }

  @Test void passwordPolicyAcceptsStrong() {
    assertTrue(PasswordPolicy.acceptable("Smoke#2026x"));
    assertTrue(PasswordPolicy.violations("Khattab#2026").isEmpty());
  }

  @Test void lockoutTriggersAtMaxFailures() {
    assertFalse(LockoutPolicy.shouldLock(LockoutPolicy.MAX_FAILURES - 1));
    assertTrue(LockoutPolicy.shouldLock(LockoutPolicy.MAX_FAILURES));
  }

  @Test void lockWindowIsRespected() {
    var now = OffsetDateTime.now();
    assertTrue(LockoutPolicy.isLocked(LockoutPolicy.lockUntil(now), now));
    assertFalse(LockoutPolicy.isLocked(now.minusMinutes(1), now));
    assertFalse(LockoutPolicy.isLocked(null, now));
  }
}
