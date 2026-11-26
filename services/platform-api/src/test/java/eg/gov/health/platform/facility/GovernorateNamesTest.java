package eg.gov.health.platform.facility;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GovernorateNamesTest {
  @Test void allTwentySevenPresent() {
    assertEquals(27, GovernorateNames.ALL.size());
    assertEquals(27, GovernorateNames.ALL.stream().map(GovernorateNames.Gov::code).distinct().count());
  }
  @Test void formsAreEquivalent() {
    assertTrue(GovernorateNames.matches("CAI", "القاهرة"));
    assertTrue(GovernorateNames.matches("القاهرة", "Cairo"));
    assertTrue(GovernorateNames.matches("SIN_N", "شمال سيناء"));
    assertTrue(GovernorateNames.matches("GIZ", "GIZ"));
  }
  @Test void differentGovernoratesDoNotMatch() {
    assertFalse(GovernorateNames.matches("CAI", "الجيزة"));
    assertFalse(GovernorateNames.matches("CAI", "GIZ"));
    assertFalse(GovernorateNames.matches("SIN_N", "SIN_S"));
  }
  @Test void unknownAndNullNeverMatch() {
    assertFalse(GovernorateNames.matches("CAI", "مكان مجهول"));
    assertFalse(GovernorateNames.matches(null, "CAI"));
    assertFalse(GovernorateNames.matches("CAI", null));
    assertNull(GovernorateNames.of(""));
  }
  @Test void directorateNamedAfterGovernorate() {
    assertEquals("مديرية صحة القاهرة", GovernorateNames.directorateAr("CAI"));
    assertEquals("مديرية صحة أسوان", GovernorateNames.directorateAr("أسوان"));
    assertNull(GovernorateNames.directorateAr("غير موجود"));
  }
}
