package eg.gov.health.platform.clinicalforms;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "form_templates")
public class FormTemplate {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String specialty;
  @Column(name="fields_spec") private String fieldsSpec;
  private String version;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FormTemplate() {}
  public FormTemplate(UUID facilityId, String governorate, String name, String specialty, String fieldsSpec, String version) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.specialty=specialty; this.fieldsSpec=fieldsSpec; this.version=version;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
