package eg.gov.health.platform.maternity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "delivery_records")
public class DeliveryRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="pregnancy_id", nullable=false) private UUID pregnancyId;
  @Column(name="labor_id") private UUID laborId;
  @Column(name="delivery_type", nullable=false) private String deliveryType;
  @Column(name="delivered_at") private OffsetDateTime deliveredAt = OffsetDateTime.now();
  @Column(name="blood_loss_ml", nullable=false) private int bloodLossMl;
  private String complications;
  @Column(name="newborn_count", nullable=false) private int newbornCount = 1;
  @Column(nullable=false) private boolean pph = false;
  @Column(name="signed_off", nullable=false) private boolean signedOff = false;
  @Column(name="signed_by") private String signedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DeliveryRecord() {}
  public DeliveryRecord(UUID facilityId, String governorate, UUID pregnancyId, UUID laborId, String deliveryType,
                        int bloodLossMl, String complications, int newbornCount) {
    this.facilityId=facilityId; this.governorate=governorate; this.pregnancyId=pregnancyId; this.laborId=laborId;
    this.deliveryType=deliveryType; this.bloodLossMl=bloodLossMl; this.complications=complications; this.newbornCount=newbornCount;
    this.pph = MaternityRules.isPph(bloodLossMl, deliveryType);
  }
  public UUID getId(){return id;} public boolean isPph(){return pph;} public boolean isSignedOff(){return signedOff;}
  public UUID getPregnancyId(){return pregnancyId;} public String getDeliveryType(){return deliveryType;}
  public int getBloodLossMl(){return bloodLossMl;} public OffsetDateTime getDeliveredAt(){return deliveredAt;}
  public void signOff(String by){ this.signedOff=true; this.signedBy=by; }
}
