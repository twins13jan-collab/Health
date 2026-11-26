package eg.gov.health.platform.modulespec;
import java.util.*;
/** Module-specification rules (module 81). A functional module
 *  is specified only when every section exists: identity,
 *  purpose, scope, owner and roles; the workflow with its
 *  states, transitions, approvals, exceptions and escalation
 *  paths; the entities with their sensitive fields, source of
 *  truth and retention; the interfaces, events, messages and
 *  idempotency keys; and the permissions, isolation scope,
 *  audit and delegation. Accreditation certifies a COMPLETE
 *  specification — a module with a missing section is a draft
 *  with ambitions. */
public final class ModuleSpecRules {
  private ModuleSpecRules() {}
  public static List<String> missingSections(String purpose, String scope, String ownerOrg, String usersRoles,
      boolean workflow, boolean entities, boolean sensitiveFields, boolean interfaces, boolean permissions) {
    List<String> missing = new ArrayList<>();
    if (purpose == null || purpose.isBlank()) missing.add("purpose");
    if (scope == null || scope.isBlank()) missing.add("scope");
    if (ownerOrg == null || ownerOrg.isBlank()) missing.add("owner");
    if (usersRoles == null || usersRoles.isBlank()) missing.add("users_roles");
    if (!workflow) missing.add("workflow_states_transitions");
    if (!entities) missing.add("entities_source_of_truth_retention");
    if (!sensitiveFields) missing.add("sensitive_fields");
    if (!interfaces) missing.add("interfaces_events_idempotency");
    if (!permissions) missing.add("permissions_isolation_audit");
    return missing;
  }
}
