package eg.gov.health.platform.neonatal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "newborn_records")
public class NewbornRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="mother_patient_id", nullable=false) private UUID motherPatientId;
  @Column(name="pregnancy_id") private UUID pregnancyId;
  @Column(name="delivery_id") private UUID deliveryId;
  @Column(name="patient_id") private UUID patientId;
  @Column(name="birth_order", nullable=false) private int birthOrder = 1;
  @Column(name="temporary_identifier", nullable=false) private boolean temporaryIdentifier = false;
  private String sex;
  @Column(name="born_at") private OffsetDateTime bornAt = OffsetDateTime.now();
  @Column(name="birth_place") private String birthPlace;
  @Column(name="birth_weight_g") private Integer birthWeightG;
  @Column(name="length_cm") private Double lengthCm;
  @Column(name="head_circumference_cm") private Double headCircumferenceCm;
  @Column(name="gestational_weeks") private Integer gestationalWeeks;
  @Column(name="apgar_1min") private Integer apgar1min;
  @Column(name="apgar_5min") private Integer apgar5min;
  @Column(nullable=false) private String status = "with_mother";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NewbornRecord() {}
  public NewbornRecord(UUID facilityId, String governorate, UUID motherPatientId, UUID pregnancyId, UUID deliveryId,
                       int birthOrder, boolean temporaryIdentifier, String sex, String birthPlace, Integer birthWeightG,
                       Double lengthCm, Double headCm, Integer gestationalWeeks, Integer apgar1, Integer apgar5) {
    this.facilityId=facilityId; this.governorate=governorate; this.motherPatientId=motherPatientId; this.pregnancyId=pregnancyId;
    this.deliveryId=deliveryId; this.birthOrder=birthOrder; this.temporaryIdentifier=temporaryIdentifier; this.sex=sex;
    this.birthPlace=birthPlace; this.birthWeightG=birthWeightG; this.lengthCm=lengthCm; this.headCircumferenceCm=headCm;
    this.gestationalWeeks=gestationalWeeks; this.apgar1min=apgar1; this.apgar5min=apgar5;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getMotherPatientId(){return motherPatientId;}
  public UUID getDeliveryId(){return deliveryId;} public Integer getBirthWeightG(){return birthWeightG;}
  public Integer getApgar5min(){return apgar5min;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
