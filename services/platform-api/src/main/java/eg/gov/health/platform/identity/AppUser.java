package eg.gov.health.platform.identity;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "app_users")
public class AppUser {
  @Id @GeneratedValue private UUID id;
  @Column(unique = true, nullable = false) private String email;
  @Column(name = "password_hash", nullable = false) private String passwordHash;
  @Column(nullable = false) private String role;
  @Column(name = "facility_id") private UUID facilityId;
  @Column(name = "patient_id") private UUID patientId;
  private String governorate; private String directorate;
  @Column(name = "authority") private String authority;
  @Column(name = "full_name") private String fullName;
  @Column(name = "mfa_enabled") private boolean mfaEnabled;
  @Column(name = "is_active") private boolean active = true;
  @Column(name = "failed_login_attempts") private int failedLoginAttempts = 0;
  @Column(name = "locked_until") private OffsetDateTime lockedUntil;
  @Column(name = "last_login_at") private OffsetDateTime lastLoginAt;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AppUser() {}
  public AppUser(String email, String passwordHash, String role, UUID facilityId, String governorate, String directorate, String fullName) {
    this.email = email; this.passwordHash = passwordHash; this.role = role; this.facilityId = facilityId;
    this.governorate = governorate; this.directorate = directorate; this.fullName = fullName;
  }
  public UUID getId() { return id; } public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; } public String getRole() { return role; }
  public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public UUID getPatientId() { return patientId; }
  public void setPatientId(UUID patientId) { this.patientId = patientId; touch(); }
  public String getDirectorate() { return directorate; } public String getFullName() { return fullName; }
  public boolean isActive() { return active; } public boolean isMfaEnabled() { return mfaEnabled; }
  public int getFailedLoginAttempts() { return failedLoginAttempts; }
  public OffsetDateTime getLockedUntil() { return lockedUntil; }
  public OffsetDateTime getLastLoginAt() { return lastLoginAt; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  // Mutators for account administration and the login-lockout flow.
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; touch(); }
  public void setRole(String role) { this.role = role; touch(); }
  public void setFacilityId(UUID facilityId) { this.facilityId = facilityId; touch(); }
  public String getAuthority() { return authority; }
  public void setAuthority(String authority) { this.authority = authority; touch(); }
  public void setGovernorate(String governorate) { this.governorate = governorate; touch(); }
  public void setDirectorate(String directorate) { this.directorate = directorate; touch(); }
  public void setFullName(String fullName) { this.fullName = fullName; touch(); }
  public void setActive(boolean active) { this.active = active; touch(); }
  public void setFailedLoginAttempts(int n) { this.failedLoginAttempts = n; }
  public void setLockedUntil(OffsetDateTime t) { this.lockedUntil = t; }
  public void setLastLoginAt(OffsetDateTime t) { this.lastLoginAt = t; }
  private void touch() { this.updatedAt = OffsetDateTime.now(); }
}
