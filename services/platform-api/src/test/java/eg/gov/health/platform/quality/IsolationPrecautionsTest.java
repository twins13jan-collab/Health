package eg.gov.health.platform.quality;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class IsolationPrecautionsTest {
  @Test void mapping() {
    assertEquals("airborne", IsolationPrecautions.precautionFor("TB"));
    assertEquals("contact", IsolationPrecautions.precautionFor("MRSA"));
    assertEquals("droplet", IsolationPrecautions.precautionFor("INFLUENZA"));
    assertEquals("standard", IsolationPrecautions.precautionFor("CELLULITIS"));
    assertTrue(IsolationPrecautions.requiresNegativePressure("MEASLES"));
    assertFalse(IsolationPrecautions.requiresNegativePressure("MRSA"));
  }
}
