package eg.gov.health.platform.partnerships;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "partner_contracts")
public class PartnerContract {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="partner_id", nullable=false) private UUID partnerId;
  private String scope;
  @Column(name="access_scope") private String accessScope;
  private String purpose;
  @Column(name="starts_on") private LocalDate startsOn;
  @Column(name="ends_on") private LocalDate endsOn;
  @Column(name="sla_ref") private String slaRef;
  @Column(name="privacy_terms", nullable=false) private boolean privacyTerms = false;
  @Column(name="security_terms", nullable=false) private boolean securityTerms = false;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PartnerContract() {}
  public PartnerContract(UUID facilityId, String governorate, UUID partnerId, String scope, String accessScope, String purpose,
                         LocalDate startsOn, LocalDate endsOn, String slaRef, boolean privacyTerms, boolean securityTerms) {
    this.facilityId=facilityId; this.governorate=governorate; this.partnerId=partnerId; this.scope=scope; this.accessScope=accessScope;
    this.purpose=purpose; this.startsOn=startsOn; this.endsOn=endsOn; this.slaRef=slaRef; this.privacyTerms=privacyTerms; this.securityTerms=securityTerms;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public boolean isPrivacyTerms(){return privacyTerms;} public boolean isSecurityTerms(){return securityTerms;}
  public String getAccessScope(){return accessScope;} public String getPurpose(){return purpose;}
  public LocalDate getStartsOn(){return startsOn;} public LocalDate getEndsOn(){return endsOn;} public UUID getPartnerId(){return partnerId;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
