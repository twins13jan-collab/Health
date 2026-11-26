package eg.gov.health.platform.identity;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.config.JwtService;
import eg.gov.health.platform.facility.*;
import eg.gov.health.platform.patient.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

/** Self-registration (public portal wave). Two doors:
 *  - Citizens/patients: an account linked to their unified
 *    patient record (found by national ID or created), active
 *    immediately, role 'patient', signed in on success.
 *  - Facilities: the facility is created 'pending' and its
 *    manager account inactive — an administrator approves both
 *    from the admin panel before either can be used. Nothing
 *    self-registered ever gains staff powers by itself. */
@RestController @RequestMapping("/api/auth")
public class RegistrationController {
  private final AppUserRepository users;
  private final PatientRepository patients;
  private final FacilityRepository facilities;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final AuditService audit;

  private final eg.gov.health.platform.settings.SettingsService settings;
  public RegistrationController(AppUserRepository users, PatientRepository patients,
                                FacilityRepository facilities, PasswordEncoder encoder,
                                JwtService jwt, AuditService audit,
                                eg.gov.health.platform.settings.SettingsService settings) {
    this.settings = settings;
    this.users = users; this.patients = patients; this.facilities = facilities;
    this.encoder = encoder; this.jwt = jwt; this.audit = audit;
  }

  private void checkPassword(String password) {
    var v = PasswordPolicy.violations(password,
        settings.getInt("security.password_min_length", PasswordPolicy.MIN_LENGTH));
    if (!v.isEmpty()) throw BizException.badRequest("password_policy", String.join("؛ ", v));
  }
  private void checkEmailFree(String email) {
    if (email == null || !email.contains("@")) throw BizException.badRequest("email_invalid", "بريد إلكتروني غير صالح");
    if (users.findByEmail(email).isPresent()) throw BizException.conflict("email_taken", "هذا البريد مسجل من قبل — سجّل الدخول");
  }

  public record PatientRegistration(String fullName, String nationalId, String email, String password,
                                    String phone, LocalDate birthDate, String gender, String governorate) {}
  @PostMapping("/register-patient")
  Map<String, Object> registerPatient(@RequestBody PatientRegistration r) {
    if (!settings.getBool("registration.patient_enabled", true))
      throw BizException.forbidden("تسجيل المواطنين متوقف مؤقتًا بقرار إدارة المنظومة");
    if (r.fullName() == null || r.fullName().isBlank()) throw BizException.badRequest("name_required", "الاسم الكامل مطلوب");
    if (r.nationalId() == null || !r.nationalId().matches("\\d{14}"))
      throw BizException.badRequest("national_id_invalid", "الرقم القومي 14 رقمًا");
    checkEmailFree(r.email());
    checkPassword(r.password());
    Patient patient;
    try {
      patient = patients.findByNationalId(r.nationalId()).orElseGet(() ->
          patients.save(new Patient(null, r.governorate(), r.nationalId(), r.fullName(),
              r.birthDate(), r.gender(), r.phone(), null, "verified")));
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      // The national ID already has a record inside a facility,
      // invisible to an anonymous caller under RLS isolation.
      throw BizException.conflict("patient_record_exists",
          "يوجد سجل طبي بهذا الرقم القومي داخل منشأة — تواصل مع منشأتك أو الإدارة لربط حسابك به");
    }
    var user = new AppUser(r.email(), encoder.encode(r.password()), "patient",
        null, r.governorate(), null, r.fullName());
    user.setPatientId(patient.getId());
    users.save(user);
    audit.record("auth.register_patient", "app_user", user.getId().toString(), "{}");
    String token = jwt.issue(user.getId().toString(), user.getEmail(), user.getRole(),
        null, user.getGovernorate(), user.getDirectorate());
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("ok", true); out.put("access_token", token); out.put("token_type", "Bearer");
    out.put("user", UserViews.of(user));
    out.put("patientId", String.valueOf(patient.getId()));
    return out;
  }

  public record FacilityRegistration(String facilityNameAr, String facilityNameEn, String type, String level,
                                     String governorate, String directorate, String city, String ownership,
                                     Integer bedCapacity, String adminFullName, String adminEmail, String adminPassword,
                                     String licenseNo, String licenseAuthority, java.time.LocalDate licenseExpiry) {}
  @PostMapping("/register-facility")
  Map<String, Object> registerFacility(@RequestBody FacilityRegistration r) {
    if (!settings.getBool("registration.facility_enabled", true))
      throw BizException.forbidden("تسجيل المنشآت متوقف مؤقتًا بقرار إدارة المنظومة");
    if (r.facilityNameAr() == null || r.facilityNameAr().isBlank())
      throw BizException.badRequest("facility_name_required", "اسم المنشأة مطلوب");
    if (r.type() == null || r.governorate() == null)
      throw BizException.badRequest("facility_fields", "نوع المنشأة والمحافظة مطلوبان");
    checkEmailFree(r.adminEmail());
    checkPassword(r.adminPassword());
    var facility = new Facility(r.facilityNameAr(), r.facilityNameEn(), r.type(),
        r.level() == null ? "primary" : r.level(), r.governorate(), r.directorate(), r.city(),
        r.ownership() == null ? "private" : r.ownership(), r.bedCapacity());
    if (r.licenseNo() != null || r.licenseAuthority() != null || r.licenseExpiry() != null)
      facility.setLicense(r.licenseNo(), r.licenseAuthority(), r.licenseExpiry());
    facility.setStatus("pending");
    facilities.save(facility);
    var admin = new AppUser(r.adminEmail(), encoder.encode(r.adminPassword()), "facility_manager",
        facility.getId(), r.governorate(), r.directorate(), r.adminFullName());
    admin.setActive(false);
    users.save(admin);
    audit.record("auth.register_facility", "facility", facility.getId().toString(), "{\"status\":\"pending\"}");
    return Map.of("ok", true, "status", "pending_approval",
        "message", "سُجلت المنشأة وحساب مديرها — الحساب يُفعَّل بعد اعتماد الإدارة");
  }

  /** Roles a person may self-register under a facility. Admin
   *  tiers are never self-registered — they are created by the
   *  tiers above them from the user-administration panel. */
  static final Set<String> STAFF_ROLES = Set.of("facility_manager",
      "receptionist", "physician", "nurse", "pharmacist", "finance_manager", "store_keeper",
      "biomedical_engineer", "oncologist", "ipc_officer", "safety_officer", "social_worker",
      "health_educator", "school_counselor", "forensic_physician");

  public record StaffRegistration(String fullName, String email, String password, String role, UUID facilityId) {}
  @PostMapping("/register-staff")
  Map<String, Object> registerStaff(@RequestBody StaffRegistration r) {
    if (r.fullName() == null || r.fullName().isBlank())
      throw BizException.badRequest("name_required", "الاسم الكامل مطلوب");
    if (r.role() == null || !STAFF_ROLES.contains(r.role()))
      throw BizException.badRequest("role_not_self_registrable", "هذا الدور لا يُسجَّل ذاتيًا — يُنشأ من إدارة المنظومة");
    if (r.facilityId() == null)
      throw BizException.badRequest("facility_required", "اختيار المنشأة التابع لها الحساب مطلوب");
    checkEmailFree(r.email());
    checkPassword(r.password());
    var facility = facilities.findById(r.facilityId())
        .orElseThrow(() -> BizException.badRequest("facility_not_found", "المنشأة المختارة غير موجودة"));
    if (!"active".equals(facility.getStatus()))
      throw BizException.badRequest("facility_not_active", "المنشأة المختارة ليست مفعّلة بعد — لا يمكن التسجيل تحتها");
    var user = new AppUser(r.email(), encoder.encode(r.password()), r.role(),
        facility.getId(), facility.getGovernorate(), facility.getDirectorate(), r.fullName());
    user.setActive(false);
    users.save(user);
    audit.record("auth.register_staff", "app_user", user.getId().toString(),
        "{\"facility\":\"" + facility.getId() + "\",\"role\":\"" + r.role() + "\"}");
    return Map.of("ok", true, "status", "pending_approval",
        "message", "تم إنشاء الحساب تحت منشأة «" + facility.getNameAr() + "» — يُفعَّل بعد اعتماد مدير المنشأة");
  }
}
