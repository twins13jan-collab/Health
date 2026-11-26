package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "suppliers")
public class Supplier {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="legal_name", nullable=false) private String legalName;
  @Column(name="registration_no") private String registrationNo;
  @Column(name="bank_account_ref") private String bankAccountRef;
  @Column(name="bank_verified", nullable=false) private boolean bankVerified = false;
  @Column(name="bank_changed_by") private String bankChangedBy;
  @Column(name="bank_verified_by") private String bankVerifiedBy;
  @Column(name="risk_level") private String riskLevel;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Supplier() {}
  public Supplier(UUID facilityId, String governorate, String legalName, String registrationNo, String bankAccountRef, String riskLevel, String changedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.legalName=legalName; this.registrationNo=registrationNo;
    this.bankAccountRef=bankAccountRef; this.riskLevel=riskLevel; this.bankChangedBy=changedBy;
  }
  public UUID getId(){return id;} public boolean isBankVerified(){return bankVerified;} public String getBankChangedBy(){return bankChangedBy;}
  public void changeBank(String ref, String by){ this.bankAccountRef=ref; this.bankChangedBy=by; this.bankVerified=false; this.bankVerifiedBy=null; this.updatedAt=OffsetDateTime.now(); }
  public void verifyBank(String by){ this.bankVerified=true; this.bankVerifiedBy=by; this.updatedAt=OffsetDateTime.now(); }
}
