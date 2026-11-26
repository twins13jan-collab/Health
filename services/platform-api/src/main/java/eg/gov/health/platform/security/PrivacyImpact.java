package eg.gov.health.platform.security;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "privacy_impacts")
public class PrivacyImpact {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(name="data_types") private String dataTypes;
  @Column(name="affected_persons_count") private Integer affectedPersonsCount;
  @Column(name="disclosure_likelihood") private String disclosureLikelihood;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PrivacyImpact() {}
  public PrivacyImpact(UUID facilityId, String governorate, UUID incidentId, String dataTypes, Integer affectedPersonsCount, String disclosureLikelihood, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.dataTypes=dataTypes;
    this.affectedPersonsCount=affectedPersonsCount; this.disclosureLikelihood=disclosureLikelihood; this.assessedBy=assessedBy;
  }
  public UUID getId(){return id;} public Integer getAffectedPersonsCount(){return affectedPersonsCount;}
}
