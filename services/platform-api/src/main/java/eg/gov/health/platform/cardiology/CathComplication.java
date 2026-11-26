package eg.gov.health.platform.cardiology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cath_complications")
public class CathComplication {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="procedure_id", nullable=false) private UUID procedureId;
  @Column(nullable=false) private String kind;
  private String severity; private String management;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CathComplication() {}
  public CathComplication(UUID facilityId, String governorate, UUID procedureId, String kind, String severity, String management) {
    this.facilityId=facilityId; this.governorate=governorate; this.procedureId=procedureId; this.kind=kind; this.severity=severity; this.management=management;
  }
  public UUID getId(){return id;}
}
