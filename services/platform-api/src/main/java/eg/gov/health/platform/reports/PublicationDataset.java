package eg.gov.health.platform.reports;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "publication_datasets")
public class PublicationDataset {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="job_id", nullable=false) private UUID jobId;
  private String fields; @Column(name="aggregation_level") private String aggregationLevel;
  @Column(nullable=false) private boolean deidentified = false;
  private String license; @Column(name="approved_by", nullable=false) private String approvedBy;
  @Column(name="published_at") private OffsetDateTime publishedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PublicationDataset() {}
  public PublicationDataset(UUID facilityId, String governorate, UUID jobId, String fields, String aggregationLevel, boolean deidentified, String license, String approvedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.jobId=jobId; this.fields=fields;
    this.aggregationLevel=aggregationLevel; this.deidentified=deidentified; this.license=license; this.approvedBy=approvedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;}
}
