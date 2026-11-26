package eg.gov.health.platform.sensory;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ent_procedures")
public class EntProcedure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="procedure_code", nullable=false) private String procedureCode;
  private String site; private String side; private String anesthesia; private String complications;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EntProcedure() {}
  public EntProcedure(UUID facilityId, String governorate, UUID patientId, String procedureCode, String site, String side,
                      String anesthesia, String complications) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.procedureCode=procedureCode;
    this.site=site; this.side=side; this.anesthesia=anesthesia; this.complications=complications;
    this.status = (complications!=null && !complications.isBlank()) ? "complicated" : "reported";
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
