package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "contracts")
public class Contract {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="po_id", nullable=false) private UUID poId;
  private String terms; @Column(nullable=false) private int version = 1;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected Contract() {}
  public Contract(UUID facilityId, String governorate, UUID poId, String terms, int version, UUID supersedesId) {
    this.facilityId=facilityId; this.governorate=governorate; this.poId=poId; this.terms=terms; this.version=version; this.supersedesId=supersedesId;
  }
  public UUID getId(){return id;} public int getVersion(){return version;}
  public void supersede(){ this.status="superseded"; }
}
