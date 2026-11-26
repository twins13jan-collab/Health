package eg.gov.health.platform.clinics;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinic_sessions")
public class ClinicSession {
  @Id @GeneratedValue private UUID id;
  @Column(name = "clinic_id", nullable = false) private UUID clinicId;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "doctor_name", nullable = false) private String doctorName;
  @Column(name = "session_date", nullable = false) private LocalDate sessionDate;
  @Column(name = "starts_at", nullable = false) private LocalTime startsAt;
  @Column(name = "ends_at", nullable = false) private LocalTime endsAt;
  @Column(name = "slot_minutes") private int slotMinutes = 15;
  @Column(nullable = false) private int capacity;
  @Column(nullable = false) private String status = "scheduled"; // scheduled, open, closed, cancelled
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ClinicSession() {}
  public ClinicSession(UUID clinicId, UUID facilityId, String governorate, String doctorName,
                       LocalDate sessionDate, LocalTime startsAt, LocalTime endsAt, int slotMinutes, int capacity) {
    this.clinicId = clinicId; this.facilityId = facilityId; this.governorate = governorate;
    this.doctorName = doctorName; this.sessionDate = sessionDate; this.startsAt = startsAt;
    this.endsAt = endsAt; this.slotMinutes = slotMinutes; this.capacity = capacity;
  }
  public UUID getId() { return id; } public UUID getClinicId() { return clinicId; }
  public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public String getDoctorName() { return doctorName; } public LocalDate getSessionDate() { return sessionDate; }
  public LocalTime getStartsAt() { return startsAt; } public LocalTime getEndsAt() { return endsAt; }
  public int getSlotMinutes() { return slotMinutes; } public int getCapacity() { return capacity; }
  public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
  /** The wall-clock start of this session in the facility's zone. */
  public OffsetDateTime startInstant(ZoneId zone) { return OffsetDateTime.of(sessionDate, startsAt, zone.getRules().getOffset(sessionDate.atTime(startsAt))); }
  public OffsetDateTime slotStart(int slotIndex, ZoneId zone) { return startInstant(zone).plusMinutes((long) slotIndex * slotMinutes); }
}
