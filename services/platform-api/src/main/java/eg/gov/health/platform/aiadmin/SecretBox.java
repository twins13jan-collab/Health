package eg.gov.health.platform.aiadmin;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/** AES-256-GCM encryption for secrets stored in the database
 *  (external AI API keys). The key itself lives only in the server
 *  environment (APP_SETTINGS_KEY, base64 of 32 bytes); the database
 *  holds iv:ciphertext pairs and can never reveal a key alone.
 *  Decryption with the wrong key fails loudly (AEAD tag), never
 *  silently. */
public final class SecretBox {
  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;
  private SecretBox() {}

  public static SecretKeySpec keyFromBase64(String base64) {
    if (base64 == null || base64.isBlank()) return null;
    byte[] raw = Base64.getDecoder().decode(base64.trim());
    if (raw.length != 32) throw new IllegalArgumentException("APP_SETTINGS_KEY must be 32 bytes (base64)");
    return new SecretKeySpec(raw, "AES");
  }

  public static String encrypt(SecretKeySpec key, String plain) {
    try {
      byte[] iv = new byte[IV_BYTES];
      new SecureRandom().nextBytes(iv);
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
      byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(ct);
    } catch (Exception e) {
      throw new IllegalStateException("encryption failed", e);
    }
  }

  public static String decrypt(SecretKeySpec key, String stored) {
    try {
      String[] parts = stored.split(":", 2);
      byte[] iv = Base64.getDecoder().decode(parts[0]);
      byte[] ct = Base64.getDecoder().decode(parts[1]);
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
      return new String(c.doFinal(ct), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("decryption failed (wrong key or corrupted value)", e);
    }
  }

  /** Display-safe mask: only the last 4 characters survive. */
  public static String mask(String plain) {
    if (plain == null || plain.isBlank()) return null;
    return plain.length() <= 4 ? "••••" : "••••••••" + plain.substring(plain.length() - 4);
  }
}
