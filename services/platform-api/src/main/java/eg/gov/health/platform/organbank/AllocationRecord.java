package eg.gov.health.platform.organbank;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "allocation_records")
public class AllocationRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="donation_id", nullable=false) private UUID donationId;
  @Column(name="recipient_ref") private String recipientRef;
  @Column(name="match_basis") private String matchBasis;
  @Column(name="allocated_by") private String allocatedBy;
  @Column(name="committee_approved", nullable=false) private boolean committeeApproved = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AllocationRecord() {}
  public AllocationRecord(UUID facilityId, String governorate, UUID donationId, String recipientRef, String matchBasis, String allocatedBy, boolean committeeApproved) {
    this.facilityId=facilityId; this.governorate=governorate; this.donationId=donationId; this.recipientRef=recipientRef;
    this.matchBasis=matchBasis; this.allocatedBy=allocatedBy; this.committeeApproved=committeeApproved;
  }
  public UUID getId(){return id;}
}
