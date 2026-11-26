package eg.gov.health.platform.refdataquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mapping_changes")
public class MappingChange {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="mapping_id", nullable=false) private UUID mappingId;
  @Column(name="old_reference_code") private String oldReferenceCode;
  @Column(name="new_reference_code") private String newReferenceCode;
  private String reason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MappingChange() {}
  public MappingChange(UUID facilityId, String governorate, UUID mappingId, String oldReferenceCode, String newReferenceCode, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.mappingId=mappingId; this.oldReferenceCode=oldReferenceCode;
    this.newReferenceCode=newReferenceCode; this.reason=reason;
  }
  public UUID getId(){return id;} public String getOldReferenceCode(){return oldReferenceCode;} public String getNewReferenceCode(){return newReferenceCode;}
}
