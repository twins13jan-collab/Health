package eg.gov.health.platform.servicecatalog;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Unified definitions & service catalogs (module 78). A
 *  national catalog of medical services, procedures, tests,
 *  specialties and availability determinants; every service
 *  linked to its resource, staff, device and consumable
 *  requirements and its delivery location; definitions, prices,
 *  insurance coverage and contracts managed separately from the
 *  clinical decision; units of measure, medical references,
 *  codes, classifications and synonyms managed; and an approval
 *  lifecycle for catalog changes with effective dates and
 *  compatibility with linked systems. */
@RestController @RequestMapping("/api/service-catalog")
public class ServiceCatalogController {
  private final CatalogServiceRepository services; private final CatalogChangeRepository changes; private final AuditService audit;
  public ServiceCatalogController(CatalogServiceRepository services, CatalogChangeRepository changes, AuditService audit) {
    this.services=services; this.changes=changes; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private CatalogService service(UUID id) {
    return services.findById(id).orElseThrow(() -> BizException.notFound("service_not_found","الخدمة غير موجودة"));
  }

  public record ServiceBody(String code, String name, String category, String resourceRequirements, String deliveryLocation,
                            Double price, String insuranceCoverage, LocalDate effectiveFrom, LocalDate effectiveTo) {}
  @PostMapping("/services")
  Map<String,Object> create(@RequestBody ServiceBody r) {
    var p = principal();
    if (!services.findByCode(r.code()).isEmpty())
      throw BizException.conflict("duplicate_code","رمز قومي واحد وتعريف واحد — الرمز مسجل من قبل ولا يُنشأ له توأم");
    var s = services.save(new CatalogService(UUID.fromString(p.facilityId()), p.governorate(), r.code(), r.name(), r.category(),
      r.resourceRequirements(), r.deliveryLocation(), r.price(), r.insuranceCoverage(), r.effectiveFrom(), r.effectiveTo()));
    return Map.of("ok",true,"service",s);
  }
  @PostMapping("/services/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var s = service(id);
    if (!CatalogRules.activatable(s.getResourceRequirements(), s.getDeliveryLocation(), s.getEffectiveFrom()))
      throw BizException.conflict("not_activatable","خدمة لا تُفعّل قبل تعريف متطلبات مواردها ومكان تقديمها وتاريخ سريانها");
    s.activate(); services.save(s);
    return Map.of("ok",true,"service",s,"note","price and coverage are administrative data — activation certifies the definition, not the tariff");
  }
  public record ChangeBody(String changeDescription, String approvedBy, LocalDate effectiveFrom) {}
  @PostMapping("/services/{id}/changes")
  Map<String,Object> change(@PathVariable UUID id, @RequestBody ChangeBody r) {
    var p = principal(); service(id);
    if (r.approvedBy()==null || r.approvedBy().isBlank() || r.effectiveFrom()==null)
      throw BizException.badRequest("change_needs_approval","تغيير كتالوج يحتاج معتمدًا مسمى وتاريخ سريان — لا تعديل صامتًا ولا أثر رجعيًا خفيًا");
    var c = changes.save(new CatalogChange(UUID.fromString(p.facilityId()), p.governorate(), id, r.changeDescription(), r.approvedBy(), r.effectiveFrom()));
    audit.record("catalog.change.approved","catalog_change",c.getId().toString(),"{}");
    return Map.of("ok",true,"change",c);
  }
  @GetMapping("/services/{id}/applicability")
  Map<String,Object> applicability(@PathVariable UUID id, @RequestParam String day) {
    principal(); var s = service(id);
    boolean app = CatalogRules.applicable(s.getStatus(), s.getEffectiveFrom(), s.getEffectiveTo(), LocalDate.parse(day));
    return Map.of("ok",true,"service_id",id.toString(),"day",day,"applicable",app);
  }
}
