package eg.gov.health.platform.modulespec;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "module_specifications")
public class ModuleSpecification {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="module_ref") private String moduleRef;
  private String purpose; private String scope;
  @Column(name="owner_org") private String ownerOrg;
  @Column(name="users_roles") private String usersRoles;
  @Column(name="workflow_documented", nullable=false) private boolean workflowDocumented = false;
  @Column(name="entities_documented", nullable=false) private boolean entitiesDocumented = false;
  @Column(name="sensitive_fields_documented", nullable=false) private boolean sensitiveFieldsDocumented = false;
  @Column(name="interfaces_documented", nullable=false) private boolean interfacesDocumented = false;
  @Column(name="permissions_documented", nullable=false) private boolean permissionsDocumented = false;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ModuleSpecification() {}
  public ModuleSpecification(UUID facilityId, String governorate, String moduleRef, String purpose, String scope, String ownerOrg, String usersRoles,
                               boolean workflowDocumented, boolean entitiesDocumented, boolean sensitiveFieldsDocumented,
                               boolean interfacesDocumented, boolean permissionsDocumented) {
    this.facilityId=facilityId; this.governorate=governorate; this.moduleRef=moduleRef; this.purpose=purpose; this.scope=scope;
    this.ownerOrg=ownerOrg; this.usersRoles=usersRoles; this.workflowDocumented=workflowDocumented; this.entitiesDocumented=entitiesDocumented;
    this.sensitiveFieldsDocumented=sensitiveFieldsDocumented; this.interfacesDocumented=interfacesDocumented; this.permissionsDocumented=permissionsDocumented;
  }
  public UUID getId(){return id;} public String getPurpose(){return purpose;} public String getScope(){return scope;}
  public String getOwnerOrg(){return ownerOrg;} public String getUsersRoles(){return usersRoles;}
  public boolean isWorkflowDocumented(){return workflowDocumented;} public boolean isEntitiesDocumented(){return entitiesDocumented;}
  public boolean isSensitiveFieldsDocumented(){return sensitiveFieldsDocumented;} public boolean isInterfacesDocumented(){return interfacesDocumented;}
  public boolean isPermissionsDocumented(){return permissionsDocumented;}
  public void accredit(){ this.status="accredited"; this.updatedAt=OffsetDateTime.now(); }
}
