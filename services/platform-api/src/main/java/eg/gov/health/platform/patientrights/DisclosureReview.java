package eg.gov.health.platform.patientrights;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "disclosure_reviews")
public class DisclosureReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="complaint_id", nullable=false) private UUID complaintId;
  private String requester; @Column(name="data_subject") private String dataSubject;
  @Column(name="privacy_review") private String privacyReview;
  @Column(name="legal_review") private String legalReview;
  @Column(nullable=false) private String status = "pending";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DisclosureReview() {}
  public DisclosureReview(UUID facilityId, String governorate, UUID complaintId, String requester, String dataSubject) {
    this.facilityId=facilityId; this.governorate=governorate; this.complaintId=complaintId; this.requester=requester; this.dataSubject=dataSubject;
  }
  public UUID getId(){return id;} public String getPrivacyReview(){return privacyReview;} public String getLegalReview(){return legalReview;}
  public String getStatus(){return status;}
  public void review(String privacy, String legal){
    if (privacy!=null) this.privacyReview=privacy; if (legal!=null) this.legalReview=legal;
    this.status = ComplaintRules.disclosureApprovable(this.privacyReview, this.legalReview) ? "approved" : "pending";
    this.updatedAt=OffsetDateTime.now(); }
}
