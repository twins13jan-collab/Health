package eg.gov.health.platform.amrdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "culture_results")
public class CultureResult {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String organism; private String antibiotic; private String result; private String specimen;
  @Column(name="method_standardized", nullable=false) private boolean methodStandardized = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CultureResult() {}
  public CultureResult(UUID facilityId, String governorate, String organism, String antibiotic, String result, String specimen, boolean methodStandardized) {
    this.facilityId=facilityId; this.governorate=governorate; this.organism=organism; this.antibiotic=antibiotic; this.result=result;
    this.specimen=specimen; this.methodStandardized=methodStandardized;
  }
  public UUID getId(){return id;} public String getResult(){return result;} public boolean isMethodStandardized(){return methodStandardized;}
}
