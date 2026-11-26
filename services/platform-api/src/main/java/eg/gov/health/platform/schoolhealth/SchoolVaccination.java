package eg.gov.health.platform.schoolhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "school_vaccinations")
public class SchoolVaccination {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="student_id", nullable=false) private UUID studentId;
  private String vaccine; private String dose; private String batch;
  @Column(name="consent_verified", nullable=false) private boolean consentVerified = false;
  @Column(name="administered_at") private OffsetDateTime administeredAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SchoolVaccination() {}
  public SchoolVaccination(UUID facilityId, String governorate, UUID studentId, String vaccine, String dose, String batch, boolean consentVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.studentId=studentId; this.vaccine=vaccine; this.dose=dose;
    this.batch=batch; this.consentVerified=consentVerified; this.administeredAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
