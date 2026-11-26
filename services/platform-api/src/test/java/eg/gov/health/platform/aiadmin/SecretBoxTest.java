package eg.gov.health.platform.aiadmin;
import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class SecretBoxTest {
  private String randomKey() {
    byte[] b = new byte[32]; new SecureRandom().nextBytes(b);
    return Base64.getEncoder().encodeToString(b);
  }
  @Test void roundtrip() {
    var key = SecretBox.keyFromBase64(randomKey());
    String enc = SecretBox.encrypt(key, "sk-or-v1-secret-1234");
    assertNotEquals("sk-or-v1-secret-1234", enc);
    assertEquals("sk-or-v1-secret-1234", SecretBox.decrypt(key, enc));
  }
  @Test void wrongKeyFailsLoudly() {
    var k1 = SecretBox.keyFromBase64(randomKey());
    var k2 = SecretBox.keyFromBase64(randomKey());
    String enc = SecretBox.encrypt(k1, "secret");
    assertThrows(IllegalStateException.class, () -> SecretBox.decrypt(k2, enc));
  }
  @Test void maskShowsOnlyLastFour() {
    assertEquals("••••••••1234", SecretBox.mask("sk-or-v1-secret-1234"));
    assertEquals("••••", SecretBox.mask("abc"));
    assertNull(SecretBox.mask(null));
    assertNull(SecretBox.mask("  "));
  }
  @Test void blankKeyMeansNotConfigured() {
    assertNull(SecretBox.keyFromBase64(""));
    assertNull(SecretBox.keyFromBase64(null));
  }
}
