package eg.gov.health.platform.labsafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "reagent_lots")
public class ReagentLot {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="introduction_id") private UUID introductionId;
  @Column(name="lot_no") private String lotNo;
  @Column(name="expiry_date") private LocalDate expiryDate;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ReagentLot() {}
  public ReagentLot(UUID facilityId, String governorate, UUID introductionId, String lotNo, LocalDate expiryDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.introductionId=introductionId; this.lotNo=lotNo; this.expiryDate=expiryDate;
  }
  public UUID getId(){return id;} public LocalDate getExpiryDate(){return expiryDate;}
}
