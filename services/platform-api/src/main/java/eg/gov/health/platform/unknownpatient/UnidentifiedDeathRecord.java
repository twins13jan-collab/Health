package eg.gov.health.platform.unknownpatient;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "unidentified_death_records")
public class UnidentifiedDeathRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="temp_id", nullable=false) private UUID tempId;
  @Column(name="legal_procedure_ref") private String legalProcedureRef;
  @Column(name="authority_notified", nullable=false) private boolean authorityNotified = false;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected UnidentifiedDeathRecord() {}
  public UnidentifiedDeathRecord(UUID facilityId, String governorate, UUID tempId, String legalProcedureRef, boolean authorityNotified) {
    this.facilityId=facilityId; this.governorate=governorate; this.tempId=tempId; this.legalProcedureRef=legalProcedureRef; this.authorityNotified=authorityNotified;
  }
  public UUID getId(){return id;}
}
