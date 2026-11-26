package eg.gov.health.platform.multilingual;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Multilingual support & usability access (module 126).
 *  Arabic with RTL direction and English supported; medical
 *  terms and translations managed; screen-reader and keyboard
 *  compatibility; contrast, fonts and accessible interfaces;
 *  low-capability devices and weak connectivity supported; and
 *  usability tested with diverse groups. */
@RestController @RequestMapping("/api/multilingual")
public class MultilingualController {
  private final MedicalTranslationRepository translations; private final AccessibilityCheckRepository checks; private final AuditService audit;
  public MultilingualController(MedicalTranslationRepository translations, AccessibilityCheckRepository checks, AuditService audit) {
    this.translations=translations; this.checks=checks; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record TranslationBody(String termKey, String language, String translatedText, Boolean medicalReviewed) {}
  @PostMapping("/translations")
  Map<String,Object> create(@RequestBody TranslationBody r) {
    var p = principal();
    var t = translations.save(new MedicalTranslation(UUID.fromString(p.facilityId()), p.governorate(), r.termKey(), r.language(),
      r.translatedText(), Boolean.TRUE.equals(r.medicalReviewed())));
    return Map.of("ok",true,"translation",t);
  }
  @PostMapping("/translations/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id) {
    principal();
    var t = translations.findById(id).orElseThrow(() -> BizException.notFound("translation_not_found","الترجمة غير موجودة"));
    if (!LanguageRules.publishable(t.isMedicalReviewed()))
      throw BizException.conflict("not_publishable","ترجمة طبية لا تُنشر قبل مراجعة طبية — تعليمات جرعة مترجمة آليًا خطر سريري بشارة لغة");
    t.publish(); translations.save(t);
    return Map.of("ok",true,"translation",t);
  }
  public record CheckBody(String surface, String checkType, Boolean passed, Boolean testedWithDiverseUsers) {}
  @PostMapping("/accessibility-checks")
  Map<String,Object> check(@RequestBody CheckBody r) {
    var p = principal();
    var c = checks.save(new AccessibilityCheck(UUID.fromString(p.facilityId()), p.governorate(), r.surface(), r.checkType(),
      Boolean.TRUE.equals(r.passed()), Boolean.TRUE.equals(r.testedWithDiverseUsers())));
    return Map.of("ok",true,"check",c,
      "accessibility_claim_valid",LanguageRules.accessibilityClaimValid(Boolean.TRUE.equals(r.passed()), Boolean.TRUE.equals(r.testedWithDiverseUsers())),
      "note","a checklist ticked by the developer alone certifies imagination, not access");
  }
}
