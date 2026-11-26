package eg.gov.health.platform.bloodbank;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/blood-bank")
public class BloodBankController {
  private final BloodUnitRepository repo; private final AuditService audit;
  public BloodBankController(BloodUnitRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record UnitRequest(String unitCode, String bloodGroup, String component, LocalDate donatedAt, LocalDate expiresAt) {}
  @PostMapping("/units")
  Map<String,Object> add(@RequestBody UnitRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.unitCode()==null||!BloodCompatibility.GROUPS.contains(r.bloodGroup()))
      throw BizException.badRequest("invalid_blood_group","فصيلة غير معروفة");
    var u = repo.save(new BloodUnit(UUID.fromString(p.facilityId()), p.governorate(), r.unitCode(), r.bloodGroup(), r.component(), r.donatedAt(), r.expiresAt()));
    audit.record("blood.unit.added","blood_unit",u.getId().toString(),"{}");
    return Map.of("ok",true,"unit",u);
  }
  @GetMapping("/compatibility/{recipientGroup}")
  Map<String,Object> compatibility(@PathVariable String recipientGroup) {
    if (!BloodCompatibility.GROUPS.contains(recipientGroup)) throw BizException.badRequest("invalid_blood_group","فصيلة غير معروفة");
    return Map.of("ok",true,"recipient",recipientGroup,"compatible_donors",BloodCompatibility.compatibleDonorsFor(recipientGroup));
  }
  @PostMapping("/units/{id}/reserve")
  Map<String,Object> reserve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var u = repo.findById(id).orElseThrow(() -> BizException.notFound("unit_not_found","الوحدة غير موجودة"));
    if (!"available".equals(u.getStatus())) throw BizException.conflict("unit_unavailable","الوحدة ليست متاحة");
    if (u.getExpiresAt()!=null && u.getExpiresAt().isBefore(LocalDate.now())) throw BizException.conflict("unit_expired","وحدة منتهية الصلاحية لا تُحجز");
    String recipientGroup = body.get("recipient_blood_group");
    if (recipientGroup==null || !BloodCompatibility.canDonateRbc(u.getBloodGroup(), recipientGroup))
      throw BizException.conflict("incompatible","فصيلة الوحدة "+u.getBloodGroup()+" غير متوافقة مع المستقبل "+recipientGroup+" (دورة 223)");
    u.reserveFor(UUID.fromString(body.get("patient_id"))); repo.save(u);
    audit.record("blood.unit.reserved","blood_unit",id.toString(),"{}");
    return Map.of("ok",true,"unit",u);
  }
  @GetMapping("/units")
  Map<String,Object> list(@RequestParam(required=false) String status) {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdAndStatus(UUID.fromString(p.facilityId()), status==null?"available":status);
    return Map.of("ok",true,"units",rows,"count",rows.size());
  }
}
