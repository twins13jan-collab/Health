package eg.gov.health.platform.mobiledevices;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Mobile apps & device identity (module 127). Devices are
 *  registered and bound to user and facility; sessions,
 *  encryption, updates and revocation are managed; secure wipe
 *  on loss or end of authorization; notifications with delivery
 *  acknowledgment; and camera, barcode, location and file
 *  controls governed. */
@RestController @RequestMapping("/api/mobile-devices")
public class MobileDevicesController {
  private final MobileDeviceRepository devices; private final DeviceSessionRepository sessions;
  private final PushReceiptRepository receipts; private final AuditService audit;
  public MobileDevicesController(MobileDeviceRepository devices, DeviceSessionRepository sessions, PushReceiptRepository receipts, AuditService audit) {
    this.devices=devices; this.sessions=sessions; this.receipts=receipts; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private MobileDevice device(UUID id) {
    return devices.findById(id).orElseThrow(() -> BizException.notFound("device_not_found","الجهاز غير موجود"));
  }

  public record DeviceBody(String deviceRef, String userRef, Integer appVersion, Integer minSupportedVersion, Boolean encryptionEnabled) {}
  @PostMapping("/devices")
  Map<String,Object> register(@RequestBody DeviceBody r) {
    var p = principal();
    var d = devices.save(new MobileDevice(UUID.fromString(p.facilityId()), p.governorate(), r.deviceRef(), r.userRef(), r.appVersion(), r.minSupportedVersion(), Boolean.TRUE.equals(r.encryptionEnabled())));
    return Map.of("ok",true,"device",d,"update_required",MobileRules.updateRequired(r.appVersion(), r.minSupportedVersion()));
  }
  @PostMapping("/devices/{id}/sessions")
  Map<String,Object> session(@PathVariable UUID id) {
    var p = principal(); var d = device(id);
    if (MobileRules.updateRequired(d.getAppVersion(), d.getMinSupportedVersion()))
      throw BizException.conflict("update_required","إصدار تطبيق دون الحد المدعوم — حدّث قبل العمل");
    if (!MobileRules.sessionAllowed(d.isEncryptionEnabled(), d.getStatus()))
      throw BizException.conflict("session_blocked","جهاز غير مشفر أو غير نشط لا يحمل جلسة سريرية");
    var s = sessions.save(new DeviceSession(UUID.fromString(p.facilityId()), p.governorate(), id));
    return Map.of("ok",true,"session",s);
  }
  public record WipeBody(String reason) {}
  @PostMapping("/devices/{id}/wipe")
  Map<String,Object> wipe(@PathVariable UUID id, @RequestBody WipeBody r) {
    principal(); var d = device(id);
    if (!MobileRules.wipeValid(r.reason()))
      throw BizException.badRequest("wipe_reason_invalid","مسح آمن لسبب مشروع فقط: فقدان أو سرقة أو انتهاء تفويض");
    d.setStatus("wiped"); devices.save(d);
    sessions.findByDeviceId(id).forEach(s -> { s.end(); sessions.save(s); });
    audit.record("mobile.device.wiped","mobile_device",id.toString(),"{}");
    return Map.of("ok",true,"device",d,"sessions_ended",true);
  }
  public record PushBody(String notificationRef, Boolean delivered, Boolean acknowledged) {}
  @PostMapping("/devices/{id}/push-receipts")
  Map<String,Object> push(@PathVariable UUID id, @RequestBody PushBody r) {
    var p = principal(); device(id);
    var rc = receipts.save(new PushReceipt(UUID.fromString(p.facilityId()), p.governorate(), id, r.notificationRef(), Boolean.TRUE.equals(r.delivered()), Boolean.TRUE.equals(r.acknowledged())));
    return Map.of("ok",true,"receipt",rc,"closed_loop",Boolean.TRUE.equals(r.delivered()) && Boolean.TRUE.equals(r.acknowledged()));
  }
}
