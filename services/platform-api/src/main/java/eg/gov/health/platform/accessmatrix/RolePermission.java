package eg.gov.health.platform.accessmatrix;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name = "role_permissions")
public class RolePermission {
  @Id @GeneratedValue private UUID id;
  @Column(name="path_prefix", nullable=false) private String pathPrefix;
  @Column(name="allowed_roles", nullable=false) private String allowedRoles;
  @Column(name="domain_ar") private String domainAr;
  protected RolePermission() {}
  public String getPathPrefix(){return pathPrefix;} public String getAllowedRoles(){return allowedRoles;} public String getDomainAr(){return domainAr;}
}
