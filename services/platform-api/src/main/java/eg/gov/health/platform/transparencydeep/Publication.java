package eg.gov.health.platform.transparencydeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "publications")
public class Publication {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title;
  @Column(name="source_ref") private String sourceRef;
  private String methodology;
  @Column(name="legal_basis") private String legalBasis;
  @Column(name="contains_sensitive", nullable=false) private boolean containsSensitive = false;
  @Column(nullable=false) private String status = "draft";
  @Column(name="correction_note") private String correctionNote;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Publication() {}
  public Publication(UUID facilityId, String governorate, String title, String sourceRef, String methodology, String legalBasis, boolean containsSensitive) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.sourceRef=sourceRef; this.methodology=methodology;
    this.legalBasis=legalBasis; this.containsSensitive=containsSensitive;
  }
  public UUID getId(){return id;} public String getSourceRef(){return sourceRef;} public String getMethodology(){return methodology;}
  public String getLegalBasis(){return legalBasis;} public boolean isContainsSensitive(){return containsSensitive;}
  public String getCorrectionNote(){return correctionNote;}
  public void publish(){ this.status="published"; this.updatedAt=OffsetDateTime.now(); }
  public void correct(String note){ this.correctionNote=note; this.updatedAt=OffsetDateTime.now(); }
  public void withdraw(String note){ this.status="withdrawn"; this.correctionNote=note; this.updatedAt=OffsetDateTime.now(); }
}
