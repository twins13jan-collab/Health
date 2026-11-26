package eg.gov.health.platform.tempworkforce;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "temp_evaluations")
public class TempEvaluation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assignment_id", nullable=false) private UUID assignmentId;
  private Integer rating; private String note;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TempEvaluation() {}
  public TempEvaluation(UUID facilityId, String governorate, UUID assignmentId, Integer rating, String note) {
    this.facilityId=facilityId; this.governorate=governorate; this.assignmentId=assignmentId; this.rating=rating; this.note=note;
  }
  public UUID getId(){return id;}
}
