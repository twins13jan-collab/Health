package eg.gov.health.platform.envhealth;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Environmental & climate health services (module 69).
 *  Weather, heat, air-quality, water and pollution data are
 *  linked from reliable sources; health risks of extreme heat,
 *  floods, storms and pollution are analysed; response plans
 *  cover affected populations; drinking-water, sewage and waste
 *  safety is followed up; and public-health alerts, awareness
 *  and community interventions are managed. */
@RestController @RequestMapping("/api/env-health")
public class EnvHealthController {
  private final EnvDataFeedRepository feeds; private final EnvRiskAlertRepository alerts;
  private final WaterSafetyCheckRepository checks; private final AuditService audit;
  public EnvHealthController(EnvDataFeedRepository feeds, EnvRiskAlertRepository alerts, WaterSafetyCheckRepository checks, AuditService audit) {
    this.feeds=feeds; this.alerts=alerts; this.checks=checks; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record FeedBody(String sourceType, String sourceName, Boolean reliable, String lastValue) {}
  @PostMapping("/feeds")
  Map<String,Object> feed(@RequestBody FeedBody r) {
    var p = principal();
    var f = feeds.save(new EnvDataFeed(UUID.fromString(p.facilityId()), p.governorate(), r.sourceType(), r.sourceName(), Boolean.TRUE.equals(r.reliable()), r.lastValue()));
    return Map.of("ok",true,"feed",f);
  }
  public record AlertBody(String hazardType, String level, String affectedPopulation, UUID feedId) {}
  @PostMapping("/alerts")
  Map<String,Object> alert(@RequestBody AlertBody r) {
    var p = principal();
    feeds.findById(r.feedId()).orElseThrow(() -> BizException.notFound("feed_not_found","المصدر غير موجود"));
    var a = alerts.save(new EnvRiskAlert(UUID.fromString(p.facilityId()), p.governorate(), r.hazardType(), r.level(), r.affectedPopulation(), r.feedId()));
    audit.record("env.alert.raised","env_risk_alert",a.getId().toString(),"{}");
    return Map.of("ok",true,"alert",a);
  }
  @PostMapping("/alerts/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id) {
    principal();
    var a = alerts.findById(id).orElseThrow(() -> BizException.notFound("alert_not_found","التنبيه غير موجود"));
    var f = feeds.findById(a.getFeedId()).orElseThrow(() -> BizException.notFound("feed_not_found","المصدر غير موجود"));
    if (!EnvHealthRules.alertPublishable(f.isReliable()))
      throw BizException.conflict("unreliable_feed","تنبيه صحة عامة رسمي من مصدر موثوق فقط — حساس رديء لا يُرعب محافظة");
    a.publish(); alerts.save(a);
    audit.record("env.alert.published","env_risk_alert",id.toString(),"{}");
    return Map.of("ok",true,"alert",a,"published",true);
  }
  @PostMapping("/alerts/{id}/activate-plan")
  Map<String,Object> activatePlan(@PathVariable UUID id) {
    principal();
    var a = alerts.findById(id).orElseThrow(() -> BizException.notFound("alert_not_found","التنبيه غير موجود"));
    a.activatePlan(); alerts.save(a);
    return Map.of("ok",true,"alert",a);
  }
  @PostMapping("/alerts/{id}/close")
  Map<String,Object> closeAlert(@PathVariable UUID id) {
    principal();
    var a = alerts.findById(id).orElseThrow(() -> BizException.notFound("alert_not_found","التنبيه غير موجود"));
    if (!EnvHealthRules.alertClosable(a.getLevel(), a.isResponsePlanActivated()))
      throw BizException.conflict("plan_inactive","تنبيه بمستوى طارئ لا يُغلق قبل تفعيل خطة الاستجابة الصحية");
    a.close(); alerts.save(a);
    return Map.of("ok",true,"alert",a);
  }
  public record CheckBody(String sourceRef, String checkType, String result) {}
  @PostMapping("/water-checks")
  Map<String,Object> check(@RequestBody CheckBody r) {
    var p = principal();
    var c = checks.save(new WaterSafetyCheck(UUID.fromString(p.facilityId()), p.governorate(), r.sourceRef(), r.checkType(), r.result()));
    audit.record("env.water.checked","water_safety_check",c.getId().toString(),"{}");
    return Map.of("ok",true,"check",c);
  }
  public record ActionBody(String actionTaken) {}
  @PostMapping("/water-checks/{id}/action")
  Map<String,Object> action(@PathVariable UUID id, @RequestBody ActionBody r) {
    principal();
    var c = checks.findById(id).orElseThrow(() -> BizException.notFound("check_not_found","الفحص غير موجود"));
    c.act(r.actionTaken()); checks.save(c);
    return Map.of("ok",true,"check",c);
  }
  @PostMapping("/water-checks/{id}/close")
  Map<String,Object> closeCheck(@PathVariable UUID id) {
    principal();
    var c = checks.findById(id).orElseThrow(() -> BizException.notFound("check_not_found","الفحص غير موجود"));
    if (!EnvHealthRules.waterCheckClosable(c.getResult(), c.getActionTaken()))
      throw BizException.conflict("action_missing","نتيجة مياه غير آمنة لا تُغلق قبل إجراء حماية فعلي (تقييد/تنبيه/معالجة)");
    c.close(); checks.save(c);
    return Map.of("ok",true,"check",c);
  }
}
