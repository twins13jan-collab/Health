package eg.gov.health.platform.pathology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pathology_reports")
public class PathologyReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="accession_id", nullable=false) private UUID accessionId;
  @Column(nullable=false) private String kind;
  @Column(name="report_text") private String reportText;
  private String diagnosis; private String classification; private String certainty;
  private String author; private String reviewer;
  @Column(nullable=false) private boolean signed = false;
  @Column(nullable=false) private int version = 1;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(name="amendment_reason") private String amendmentReason;
  @Column(nullable=false) private String status = "current";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PathologyReport() {}
  public PathologyReport(UUID facilityId, String governorate, UUID accessionId, String kind, String reportText, String diagnosis,
                         String classification, String certainty, String author, String reviewer, boolean signed, int version,
                         UUID supersedesId, String amendmentReason) {
    this.facilityId=facilityId; this.governorate=governorate; this.accessionId=accessionId; this.kind=kind; this.reportText=reportText;
    this.diagnosis=diagnosis; this.classification=classification; this.certainty=certainty; this.author=author; this.reviewer=reviewer;
    this.signed=signed; this.version=version; this.supersedesId=supersedesId; this.amendmentReason=amendmentReason;
  }
  public UUID getId(){return id;} public String getKind(){return kind;} public String getStatus(){return status;} public int getVersion(){return version;}
  public UUID getAccessionId(){return accessionId;} public UUID getFacilityId(){return facilityId;}
  public void supersede(){ this.status="superseded"; }
}
