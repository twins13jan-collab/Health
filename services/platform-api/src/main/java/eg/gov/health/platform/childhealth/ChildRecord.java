package eg.gov.health.platform.childhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "child_records")
public class ChildRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="child_ref") private String childRef;
  @Column(name="birth_date") private LocalDate birthDate;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ChildRecord() {}
  public ChildRecord(UUID facilityId, String governorate, String childRef, LocalDate birthDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.childRef=childRef; this.birthDate=birthDate;
  }
  public UUID getId(){return id;}
}
