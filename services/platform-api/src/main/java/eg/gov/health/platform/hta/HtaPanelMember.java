package eg.gov.health.platform.hta;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "hta_panel_members")
public class HtaPanelMember {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assessment_id", nullable=false) private UUID assessmentId;
  @Column(name="member_name") private String memberName;
  @Column(name="coi_declared", nullable=false) private boolean coiDeclared = false;
  @Column(name="coi_detail") private String coiDetail;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected HtaPanelMember() {}
  public HtaPanelMember(UUID facilityId, String governorate, UUID assessmentId, String memberName, boolean coiDeclared, String coiDetail) {
    this.facilityId=facilityId; this.governorate=governorate; this.assessmentId=assessmentId; this.memberName=memberName;
    this.coiDeclared=coiDeclared; this.coiDetail=coiDetail;
  }
  public UUID getId(){return id;}
}
