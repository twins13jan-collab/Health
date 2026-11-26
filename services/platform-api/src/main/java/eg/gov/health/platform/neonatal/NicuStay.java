package eg.gov.health.platform.neonatal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nicu_stays")
public class NicuStay {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="newborn_id", nullable=false) private UUID newbornId;
  @Column(nullable=false) private String reason;
  @Column(name="respiratory_support") private String respiratorySupport;
  @Column(name="feeding_type") private String feedingType;
  @Column(name="handover_notes") private String handoverNotes;
  @Column(nullable=false) private String status = "active";
  @Column(name="started_at") private OffsetDateTime startedAt = OffsetDateTime.now();
  @Column(name="ended_at") private OffsetDateTime endedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NicuStay() {}
  public NicuStay(UUID facilityId, String governorate, UUID newbornId, String reason, String respiratorySupport, String feedingType, String handoverNotes) {
    this.facilityId=facilityId; this.governorate=governorate; this.newbornId=newbornId; this.reason=reason;
    this.respiratorySupport=respiratorySupport; this.feedingType=feedingType; this.handoverNotes=handoverNotes;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getNewbornId(){return newbornId;}
  public String getReason(){return reason;} public String getRespiratorySupport(){return respiratorySupport;}
  public String getFeedingType(){return feedingType;} public String getHandoverNotes(){return handoverNotes;}
  public OffsetDateTime getStartedAt(){return startedAt;}
  public void end(String to, String handover){ this.status=to; this.handoverNotes=handover; this.endedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
