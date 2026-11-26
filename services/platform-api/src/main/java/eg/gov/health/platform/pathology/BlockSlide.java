package eg.gov.health.platform.pathology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "blocks_slides")
public class BlockSlide {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="accession_id", nullable=false) private UUID accessionId;
  @Column(name="block_code") private String blockCode;
  @Column(name="slide_code") private String slideCode;
  private String stain;
  @Column(name="image_ref") private String imageRef;
  @Column(nullable=false) private String status = "prepared";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected BlockSlide() {}
  public BlockSlide(UUID facilityId, String governorate, UUID accessionId, String blockCode, String slideCode, String stain, String imageRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.accessionId=accessionId; this.blockCode=blockCode;
    this.slideCode=slideCode; this.stain=stain; this.imageRef=imageRef;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void ready(){ this.status="ready"; }
}
