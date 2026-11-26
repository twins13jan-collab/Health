package eg.gov.health.platform.publicapi;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BookableServicesTest {
  @Test void catalogIsCompleteAndUnique() {
    var keys = BookableServices.CATALOG.stream().map(BookableServices.Bookable::key).toList();
    assertEquals(keys.size(), new HashSet<>(keys).size());
    assertTrue(keys.containsAll(List.of("clinics", "lab", "pharmacy", "radiology", "dialysis",
        "home_visit", "vaccination", "blood", "emergency")));
  }
  @Test void requestServicesFileAsKnownPortalTypes() {
    var allowed = Set.of("lab", "pharmacy", "radiology", "dialysis", "home_visit", "other");
    for (var b : BookableServices.CATALOG)
      if ("request".equals(b.mode())) assertTrue(allowed.contains(b.portalType()), b.key());
    // session and info services file nothing in the inbox
    assertNull(BookableServices.byKey("clinics").orElseThrow().portalType());
    assertNull(BookableServices.byKey("emergency").orElseThrow().portalType());
  }
  @Test void hospitalOffersTheFullInHospitalSet() {
    var s = BookableServices.serviceKeysOf("hospital");
    assertTrue(s.containsAll(List.of("clinics", "lab", "pharmacy", "radiology", "dialysis", "blood", "emergency")));
  }
  @Test void specializedFacilitiesOfferTheirOwnService() {
    assertTrue(BookableServices.offers("radiology_center", "radiology"));
    assertFalse(BookableServices.offers("radiology_center", "pharmacy"));
    assertTrue(BookableServices.offers("pharmacy", "pharmacy"));
    assertTrue(BookableServices.offers("dialysis_center", "dialysis"));
    assertTrue(BookableServices.offers("health_unit", "vaccination"));
    assertTrue(BookableServices.offers("mobile_clinic", "clinics"));
  }
  @Test void ambulanceStationHonestlyOffersNothingBookable() {
    assertTrue(BookableServices.serviceKeysOf("ambulance_station").isEmpty());
    assertTrue(BookableServices.serviceKeysOf(null).isEmpty());
    assertFalse(BookableServices.offers("hospital", "nonexistent"));
  }
}
