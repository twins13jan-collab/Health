package eg.gov.health.platform.connecteddevices;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Connected medical measurement & monitoring devices
 *  (module 128). A registry of devices, their measurement
 *  source and linked patient; readings received with unit,
 *  time and device verified; signal quality, anomalous values
 *  and duplicates managed; calibration and validity governed;
 *  alerts run on approved thresholds; and raw readings, their
 *  conversions and provenance are preserved. */
@RestController @RequestMapping("/api/connected-devices")
public class ConnectedDevicesController {
  private final ConnectedDeviceRepository devices; private final DeviceReadingRepository readings; private final AuditService audit;
  public ConnectedDevicesController(ConnectedDeviceRepository devices, DeviceReadingRepository readings, AuditService audit) {
    this.devices=devices; this.readings=readings; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ConnectedDevice device(UUID id) {
    return devices.findById(id).orElseThrow(() -> BizException.notFound("device_not_found","الجهاز غير موجود"));
  }

  public record DeviceBody(String deviceRef, String patientRef, LocalDate calibrationDue) {}
  @PostMapping("/devices")
  Map<String,Object> create(@RequestBody DeviceBody r) {
    var p = principal();
    var d = devices.save(new ConnectedDevice(UUID.fromString(p.facilityId()), p.governorate(), r.deviceRef(), r.patientRef(), r.calibrationDue()));
    return Map.of("ok",true,"device",d);
  }
  public record ReadingBody(String metric, Double rawValue, String unit, String signalQuality) {}
  @PostMapping("/devices/{id}/readings")
  Map<String,Object> reading(@PathVariable UUID id, @RequestBody ReadingBody r) {
    var p = principal(); var d = device(id);
    boolean calValid = DeviceRules.calibrationValid(d.getCalibrationDue(), LocalDate.now());
    boolean plausible = DeviceRules.plausible(r.metric(), r.rawValue() == null ? 0 : r.rawValue());
    boolean reliable = DeviceRules.reliable(calValid, r.signalQuality(), plausible);
    var rd = readings.save(new DeviceReading(UUID.fromString(p.facilityId()), p.governorate(), id, r.metric(), r.rawValue(), r.unit(),
      r.signalQuality(), reliable, !plausible));
    if (!reliable) audit.record("device.reading.unreliable","device_reading",rd.getId().toString(),"{}");
    return Map.of("ok",true,"reading",rd,"reliable",reliable,"anomaly",!plausible,
      "alert_eligible",reliable,"note","the raw reading is preserved with provenance — only trustworthy readings steer care or fire alerts");
  }
}
