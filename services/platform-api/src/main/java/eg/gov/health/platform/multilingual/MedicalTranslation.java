package eg.gov.health.platform.multilingual;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "medical_translations")
public class MedicalTranslation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="term_key") private String termKey;
  private String language;
  @Column(name="translated_text") private String translatedText;
  @Column(name="medical_reviewed", nullable=false) private boolean medicalReviewed = false;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MedicalTranslation() {}
  public MedicalTranslation(UUID facilityId, String governorate, String termKey, String language, String translatedText, boolean medicalReviewed) {
    this.facilityId=facilityId; this.governorate=governorate; this.termKey=termKey; this.language=language;
    this.translatedText=translatedText; this.medicalReviewed=medicalReviewed;
  }
  public UUID getId(){return id;} public boolean isMedicalReviewed(){return medicalReviewed;}
  public void publish(){ this.status="published"; this.updatedAt=OffsetDateTime.now(); }
}
