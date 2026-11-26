package eg.gov.health.platform.settings;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsCatalogTest {

  @Test void catalogueCoversEveryGroup() {
    var groups = SettingsCatalog.all().stream().map(SettingsCatalog.Spec::group).distinct().toList();
    assertTrue(groups.containsAll(java.util.List.of("identity", "contact", "security", "registration", "system", "public")));
    assertEquals(14, SettingsCatalog.all().size());
  }

  @Test void unknownKeysAreRefused() {
    assertFalse(SettingsCatalog.known("site.unknown"));
    assertNotNull(SettingsCatalog.validate("site.unknown", "x"));
    assertNotNull(SettingsCatalog.validate("security.lockout_minutes", null));
  }

  @Test void boolValuesMustBeTrueOrFalse() {
    assertNull(SettingsCatalog.validate("system.maintenance_mode", "true"));
    assertNull(SettingsCatalog.validate("registration.patient_enabled", "false"));
    assertNotNull(SettingsCatalog.validate("system.maintenance_mode", "yes"));
    assertNotNull(SettingsCatalog.validate("system.maintenance_mode", "1"));
  }

  @Test void intValuesAreParsedAndRanged() {
    assertNull(SettingsCatalog.validate("security.lockout_max_failures", "7"));
    assertNotNull(SettingsCatalog.validate("security.lockout_max_failures", "0"));
    assertNotNull(SettingsCatalog.validate("security.lockout_max_failures", "21"));
    assertNotNull(SettingsCatalog.validate("security.lockout_minutes", "abc"));
    assertNull(SettingsCatalog.validate("security.password_min_length", "12"));
    assertNotNull(SettingsCatalog.validate("security.password_min_length", "4"));
  }

  @Test void publicFlagsSeparatePublicFromInternal() {
    assertTrue(SettingsCatalog.isPublic("site.name_ar"));
    assertTrue(SettingsCatalog.isPublic("system.maintenance_mode"));
    assertFalse(SettingsCatalog.isPublic("security.lockout_minutes"));
    assertFalse(SettingsCatalog.isPublic("registration.patient_enabled"));
  }
}
