package eg.gov.health.platform.traceability;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "responsibility_transfers")
public class ResponsibilityTransfer {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="data_entity") private String dataEntity;
  @Column(name="from_level") private String fromLevel;
  @Column(name="to_level") private String toLevel;
  @Column(name="authorized_roles") private String authorizedRoles;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ResponsibilityTransfer() {}
  public ResponsibilityTransfer(UUID facilityId, String governorate, String dataEntity, String fromLevel, String toLevel, String authorizedRoles) {
    this.facilityId=facilityId; this.governorate=governorate; this.dataEntity=dataEntity; this.fromLevel=fromLevel; this.toLevel=toLevel;
    this.authorizedRoles=authorizedRoles;
  }
  public UUID getId(){return id;}
}
