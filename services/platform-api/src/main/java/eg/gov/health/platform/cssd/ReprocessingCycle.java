package eg.gov.health.platform.cssd;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity(name = "CssdReprocessingCycle") @Table(name = "cssd_reprocessing_cycles")
public class ReprocessingCycle {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="set_id", nullable=false) private UUID setId;
  @Column(name="received_from") private String receivedFrom;
  @Column(name="sorted_by") private String sortedBy;
  @Column(name="cleaning_done", nullable=false) private boolean cleaningDone = false;
  @Column(name="disinfection_done", nullable=false) private boolean disinfectionDone = false;
  @Column(nullable=false) private boolean dried = false;
  @Column(nullable=false) private boolean inspected = false;
  @Column(name="packed_by") private String packedBy;
  @Column(name="sterilizer_code") private String sterilizerCode;
  @Column(name="cycle_no") private String cycleNo;
  @Column(name="chemical_indicator") private String chemicalIndicator;
  @Column(name="biological_indicator") private String biologicalIndicator;
  @Column(name="released_by") private String releasedBy;
  @Column(name="released_at") private OffsetDateTime releasedAt;
  @Column(nullable=false) private String status = "in_progress";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ReprocessingCycle() {}
  public ReprocessingCycle(UUID facilityId, String governorate, UUID setId, String receivedFrom, String sortedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.setId=setId; this.receivedFrom=receivedFrom; this.sortedBy=sortedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getSetId(){return setId;}
  public String getReceivedFrom(){return receivedFrom;} public String getSterilizerCode(){return sterilizerCode;}
  public String getCycleNo(){return cycleNo;} public OffsetDateTime getReleasedAt(){return releasedAt;}
  public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
  public boolean isCleaningDone(){return cleaningDone;} public boolean isDisinfectionDone(){return disinfectionDone;}
  public boolean isDried(){return dried;} public boolean isInspected(){return inspected;} public String getPackedBy(){return packedBy;}
  public String getChemicalIndicator(){return chemicalIndicator;} public String getBiologicalIndicator(){return biologicalIndicator;}
  public void recordSteps(boolean cleaning, boolean disinfection, boolean dried, boolean inspected, String packedBy, String sterilizerCode, String cycleNo){
    this.cleaningDone=cleaning; this.disinfectionDone=disinfection; this.dried=dried; this.inspected=inspected;
    this.packedBy=packedBy; this.sterilizerCode=sterilizerCode; this.cycleNo=cycleNo; this.updatedAt=OffsetDateTime.now(); }
  public void recordIndicators(String chemical, String biological){ this.chemicalIndicator=chemical; this.biologicalIndicator=biological; this.updatedAt=OffsetDateTime.now(); }
  public void release(String by){ this.releasedBy=by; this.releasedAt=OffsetDateTime.now(); this.status="released"; this.updatedAt=OffsetDateTime.now(); }
  public void quarantine(){ this.status="quarantined"; this.updatedAt=OffsetDateTime.now(); }
  public void fail(){ this.status="failed"; this.updatedAt=OffsetDateTime.now(); }
}
