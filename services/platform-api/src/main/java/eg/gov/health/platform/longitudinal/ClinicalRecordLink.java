package eg.gov.health.platform.longitudinal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinical_record_links")
public class ClinicalRecordLink {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="identity_id", nullable=false) private UUID identityId;
  @Column(name="record_type", nullable=false) private String recordType;
  @Column(name="record_ref", nullable=false) private String recordRef;
  @Column(name="owner_facility") private String ownerFacility;
  @Column(name="source_system") private String sourceSystem;
  @Column(name="field_name") private String fieldName;
  @Column(name="field_value") private String fieldValue;
  @Column(name="recorded_at") private OffsetDateTime recordedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ClinicalRecordLink() {}
  public ClinicalRecordLink(UUID facilityId, String governorate, UUID identityId, String recordType, String recordRef, String ownerFacility, String sourceSystem, String fieldName, String fieldValue) {
    this.facilityId=facilityId; this.governorate=governorate; this.identityId=identityId; this.recordType=recordType;
    this.recordRef=recordRef; this.ownerFacility=ownerFacility; this.sourceSystem=sourceSystem;
    this.fieldName=fieldName; this.fieldValue=fieldValue;
  }
  public UUID getId(){return id;} public UUID getIdentityId(){return identityId;} public String getFieldName(){return fieldName;}
  public String getFieldValue(){return fieldValue;} public String getSourceSystem(){return sourceSystem;}
  public void repoint(UUID newIdentity){ this.identityId=newIdentity; }
}
