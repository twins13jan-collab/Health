package eg.gov.health.platform.medicalassets;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Fault reporting is a CLINICAL act — the nurse or physician at
 *  the bedside reports the fault; biomedical engineering owns
 *  everything after it. Reports therefore live at their own
 *  top-level path whose access-matrix row names the clinical
 *  reporters (V107), instead of under the biomedical-only
 *  /api/medical-assets row. Same rules as the nested endpoint:
 *  a patient-affecting fault requires a linked patient-safety
 *  case, the device leaves service immediately, and the reporter
 *  can never return it to service — release is biomedical/
 *  quality only. */
@RestController
@RequestMapping("/api/device-incidents")
public class DeviceIncidentController {
  private final MedicalAssetRepository assets; private final DeviceIncidentRepository incidents;
  private final AssetLifecycleEventRepository events; private final AuditService audit;
  public DeviceIncidentController(MedicalAssetRepository assets, DeviceIncidentRepository incidents,
                                  AssetLifecycleEventRepository events, AuditService audit) {
    this.assets = assets; this.incidents = incidents; this.events = events; this.audit = audit;
  }
  public record ReportRequest(UUID assetId, String description, Boolean patientAffected, String safetyCaseRef) {}
  @PostMapping
  Map<String,Object> report(@RequestBody ReportRequest r) {
    var p = TenantContext.get();
    if (p == null) throw BizException.forbidden("يتطلب تسجيل الدخول");
    if (r.assetId() == null) throw BizException.badRequest("asset_required", "معرف الجهاز مطلوب");
    var a = assets.findById(r.assetId())
        .orElseThrow(() -> BizException.notFound("asset_not_found", "الجهاز غير موجود"));
    boolean affected = Boolean.TRUE.equals(r.patientAffected());
    if (!AssetRules.safetyCaseRequired(affected, r.safetyCaseRef()))
      throw BizException.badRequest("safety_case_required", "عطل مؤثر على مريض يفتح بلاغ سلامة مرتبطًا بالحالة — مرجع البلاغ مطلوب");
    var inc = incidents.save(new DeviceIncident(a.getFacilityId(), p.governorate(), a.getId(), p.email(), r.description(), affected, r.safetyCaseRef()));
    a.setStatus("out_of_service"); assets.save(a);
    events.save(new AssetLifecycleEvent(a.getFacilityId(), null, a.getId(), "maintenance", "device incident reported; device out of service", p.email()));
    audit.record("asset.incident.reported", "device_incident", inc.getId().toString(), "{}");
    return Map.of("ok", true, "incident", inc, "asset_status", "out_of_service",
        "note", "the reporter cannot return this device to service — release is biomedical/quality only");
  }
}
