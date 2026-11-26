package eg.gov.health.platform.masterdata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "reference_codes")
public class ReferenceCode {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="code_system", nullable=false) private String codeSystem;
  @Column(nullable=false) private String code;
  private String description; private String domain;
  @Column(nullable=false) private boolean national = true;
  @Column(nullable=false) private String status = "active";
  @Column(name="effective_from") private LocalDate effectiveFrom; @Column(name="effective_to") private LocalDate effectiveTo;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ReferenceCode() {}
  public ReferenceCode(UUID facilityId, String governorate, String codeSystem, String code, String description, String domain, boolean national, String status) {
    this.facilityId=facilityId; this.governorate=governorate; this.codeSystem=codeSystem; this.code=code;
    this.description=description; this.domain=domain; this.national=national;
    this.status = status==null ? "active" : status;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public boolean isNational(){return national;}
  public String getCode(){return code;} public String getCodeSystem(){return codeSystem;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
