package eg.gov.health.platform.medicalrecords;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
/** The closing summary of one encounter, owned by Medical Records
 *  (Phase 4.9). Distinct from admissionsdeep.DischargeSummary, which
 *  documents an inpatient admission request; this summary closes an
 *  encounter file of any type. A draft may be revised; once final it
 *  is the version of record and no longer editable here. */
@Entity @Table(name = "encounter_summaries")
public class EncounterSummary {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="encounter_id", nullable=false, unique=true) private UUID encounterId;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="summary_text", nullable=false) private String summaryText;
  private String author;
  @Column(nullable=false) private String status = "draft"; // draft, final
  @Column(name="finalized_at") private OffsetDateTime finalizedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EncounterSummary() {}
  public EncounterSummary(UUID facilityId, String governorate, UUID encounterId, UUID patientId, String summaryText, String author) {
    this.facilityId=facilityId; this.governorate=governorate; this.encounterId=encounterId;
    this.patientId=patientId; this.summaryText=summaryText; this.author=author;
  }
  public UUID getId(){return id;} public UUID getEncounterId(){return encounterId;}
  public String getSummaryText(){return summaryText;} public String getAuthor(){return author;}
  public String getStatus(){return status;} public OffsetDateTime getFinalizedAt(){return finalizedAt;}
  public void revise(String text, String author){ this.summaryText=text; this.author=author; this.updatedAt=OffsetDateTime.now(); }
  public void finalizeSummary(){ this.status="final"; this.finalizedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
