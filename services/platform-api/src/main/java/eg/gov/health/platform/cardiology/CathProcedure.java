package eg.gov.health.platform.cardiology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cath_procedures")
public class CathProcedure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="booking_id", nullable=false) private UUID bookingId;
  private String findings; private String operator;
  @Column(name="contrast_volume_ml") private Integer contrastVolumeMl;
  @Column(name="started_at") private OffsetDateTime startedAt;
  @Column(name="ended_at") private OffsetDateTime endedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CathProcedure() {}
  public CathProcedure(UUID facilityId, String governorate, UUID bookingId, String findings, String operator, Integer contrastVolumeMl) {
    this.facilityId=facilityId; this.governorate=governorate; this.bookingId=bookingId; this.findings=findings;
    this.operator=operator; this.contrastVolumeMl=contrastVolumeMl; this.startedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public Integer getContrastVolumeMl(){return contrastVolumeMl;}
  public UUID getFacilityId(){return facilityId;}
}
