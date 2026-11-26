package eg.gov.health.platform.organbank;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "graft_outcomes")
public class GraftOutcome {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="donation_id", nullable=false) private UUID donationId;
  @Column(name="recipient_ref") private String recipientRef;
  private String outcome; @Column(name="follow_up_note") private String followUpNote;
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected GraftOutcome() {}
  public GraftOutcome(UUID facilityId, String governorate, UUID donationId, String recipientRef, String outcome, String followUpNote, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.donationId=donationId; this.recipientRef=recipientRef;
    this.outcome=outcome; this.followUpNote=followUpNote; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;}
}
