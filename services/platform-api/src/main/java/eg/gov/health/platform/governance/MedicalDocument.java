package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "medical_documents")
public class MedicalDocument {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String category;
  @Column(nullable=false) private String title;
  @Column(name="document_date", nullable=false) private LocalDate documentDate;
  @Column(name="storage_uri") private String storageUri;
  @Column(nullable=false) private String status = "active"; // active, archived, disposed
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MedicalDocument() {}
  public MedicalDocument(UUID facilityId, String governorate, UUID patientId, String category, String title, LocalDate documentDate, String storageUri) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.category=category;
    this.title=title; this.documentDate=documentDate; this.storageUri=storageUri;
  }
  public UUID getId(){return id;} public LocalDate getDocumentDate(){return documentDate;} public String getCategory(){return category;}
  public String getStatus(){return status;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
