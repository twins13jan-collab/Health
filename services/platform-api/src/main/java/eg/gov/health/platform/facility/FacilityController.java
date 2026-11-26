package eg.gov.health.platform.facility;
import eg.gov.health.platform.common.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/facilities")
public class FacilityController {
  private final FacilityRepository repo; private final AuditService audit;
  public FacilityController(FacilityRepository repo, AuditService audit) { this.repo = repo; this.audit = audit; }
  public record FacilityRequest(@NotBlank String nameAr, String nameEn, @NotBlank String type, @NotBlank String level,
                                @NotBlank String governorate, String directorate, String city, String ownership, Integer bedCapacity) {}
  @GetMapping
  Map<String, Object> list(@RequestParam(required = false) String governorate) {
    var rows = governorate == null ? repo.findAll() : repo.findByGovernorate(governorate);
    return Map.of("ok", true, "facilities", rows, "count", rows.size());
  }
  @GetMapping("/{id}")
  Facility get(@PathVariable UUID id) { return repo.findById(id).orElseThrow(() -> BizException.notFound("facility_not_found", "المنشأة غير موجودة")); }
  @PostMapping @PreAuthorize("hasAnyRole('national_admin','governorate_admin','directorate_admin')")
  Map<String, Object> create(@RequestBody @jakarta.validation.Valid FacilityRequest r) {
    var f = repo.save(new Facility(r.nameAr(), r.nameEn(), r.type(), r.level(), r.governorate(), r.directorate(), r.city(), r.ownership(), r.bedCapacity()));
    audit.record("facility.created", "facility", f.getId().toString(), "{}");
    return Map.of("ok", true, "id", f.getId(), "facility", f);
  }
}
