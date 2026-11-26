package eg.gov.health.platform.facility;
import java.util.*;
/** The platform's canonical facility-type taxonomy — the complete
 *  list offered by the sign-in and registration hierarchies and
 *  used to label facilities everywhere. Mirrors the web client's
 *  FACILITY_TYPE_LABELS. One entry per distinct type: "laboratory"
 *  was a stored synonym of "lab" with the identical label (the
 *  duplication the finder showed) and is merged into "lab" here;
 *  BookableServices and the finder's type filter still recognise
 *  the legacy value on stored rows. */
public final class FacilityTypes {
  private FacilityTypes() {}
  public record TypeLabel(String value, String nameAr, String nameEn) {}
  public static final List<TypeLabel> ALL = List.of(
      new TypeLabel("hospital", "مستشفى", "Hospital"),
      new TypeLabel("general_hospital", "مستشفى عام", "General hospital"),
      new TypeLabel("university_hospital", "مستشفى جامعي", "University hospital"),
      new TypeLabel("emergency_hospital", "مستشفى طوارئ", "Emergency hospital"),
      new TypeLabel("specialized_center", "مركز متخصص", "Specialized centre"),
      new TypeLabel("center", "مركز طبي", "Medical centre"),
      new TypeLabel("radiology_center", "مركز أشعة", "Radiology / imaging centre"),
      new TypeLabel("clinic", "عيادة", "Clinic"),
      new TypeLabel("health_center", "مركز صحي", "Health centre"),
      new TypeLabel("health_unit", "وحدة صحية", "Health unit"),
      new TypeLabel("family_center", "مركز صحة أسرة", "Family health centre"),
      new TypeLabel("lab", "معمل تحاليل", "Laboratory"),
      new TypeLabel("pharmacy", "صيدلية", "Pharmacy"),
      new TypeLabel("blood_bank", "بنك دم", "Blood bank"),
      new TypeLabel("dialysis_center", "مركز غسيل كلوي", "Dialysis centre"),
      new TypeLabel("oncology_center", "مركز أورام", "Oncology centre"),
      new TypeLabel("ambulance_station", "محطة إسعاف", "Ambulance station"),
      new TypeLabel("mobile_clinic", "عيادة متنقلة", "Mobile clinic"));
}
