package eg.gov.health.platform.clinicalforms;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "form_instances")
public class FormInstance {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="template_id", nullable=false) private UUID templateId;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="author_ref") private String authorRef;
  @Column(nullable=false) private boolean signed = false;
  @Column(name="structured_data", nullable=false) private boolean structuredData = false;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FormInstance() {}
  public FormInstance(UUID facilityId, String governorate, UUID templateId, String patientRef, String authorRef, UUID supersedesId) {
    this.facilityId=facilityId; this.governorate=governorate; this.templateId=templateId; this.patientRef=patientRef; this.authorRef=authorRef;
    this.supersedesId=supersedesId;
  }
  public UUID getId(){return id;} public boolean isSigned(){return signed;} public String getStatus(){return status;}
  public void sign(){ this.signed=true; this.updatedAt=OffsetDateTime.now(); }
  public void file(){ this.status="filed"; this.structuredData=true; this.updatedAt=OffsetDateTime.now(); }
  public void supersede(){ this.status="superseded"; this.updatedAt=OffsetDateTime.now(); }
}
