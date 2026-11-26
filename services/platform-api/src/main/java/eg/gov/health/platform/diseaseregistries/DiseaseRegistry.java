package eg.gov.health.platform.diseaseregistries;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "disease_registries")
public class DiseaseRegistry {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="case_definition_ref") private String caseDefinitionRef;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DiseaseRegistry() {}
  public DiseaseRegistry(UUID facilityId, String governorate, String name, String caseDefinitionRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.caseDefinitionRef=caseDefinitionRef;
  }
  public UUID getId(){return id;}
}
