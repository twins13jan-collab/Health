package eg.gov.health.platform.refdataquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "code_mappings")
public class CodeMapping {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="local_code") private String localCode;
  @Column(name="reference_code") private String referenceCode;
  @Column(name="code_system") private String codeSystem;
  @Column(nullable=false) private boolean sensitive = false;
  @Column(name="human_reviewed", nullable=false) private boolean humanReviewed = false;
  @Column(nullable=false) private boolean active = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CodeMapping() {}
  public CodeMapping(UUID facilityId, String governorate, String localCode, String referenceCode, String codeSystem,
                       boolean sensitive, boolean humanReviewed) {
    this.facilityId=facilityId; this.governorate=governorate; this.localCode=localCode; this.referenceCode=referenceCode;
    this.codeSystem=codeSystem; this.sensitive=sensitive; this.humanReviewed=humanReviewed;
  }
  public UUID getId(){return id;} public boolean isSensitive(){return sensitive;} public boolean isHumanReviewed(){return humanReviewed;}
  public String getReferenceCode(){return referenceCode;}
  public void activate(){ this.active=true; this.updatedAt=OffsetDateTime.now(); }
  public void remap(String newReferenceCode){ this.referenceCode=newReferenceCode; this.updatedAt=OffsetDateTime.now(); }
}
