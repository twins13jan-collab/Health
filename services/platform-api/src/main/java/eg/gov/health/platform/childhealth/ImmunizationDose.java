package eg.gov.health.platform.childhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "immunization_doses")
public class ImmunizationDose {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="child_id", nullable=false) private UUID childId;
  private String vaccine;
  @Column(name="batch_ref") private String batchRef;
  @Column(name="cold_chain_ok", nullable=false) private boolean coldChainOk = false;
  @Column(name="given_date") private LocalDate givenDate;
  @Column(nullable=false) private boolean valid = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ImmunizationDose() {}
  public ImmunizationDose(UUID facilityId, String governorate, UUID childId, String vaccine, String batchRef, boolean coldChainOk, LocalDate givenDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.childId=childId; this.vaccine=vaccine; this.batchRef=batchRef;
    this.coldChainOk=coldChainOk; this.givenDate=givenDate; this.valid = coldChainOk;
  }
  public UUID getId(){return id;} public String getVaccine(){return vaccine;} public boolean isValid(){return valid;}
}
