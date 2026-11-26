package eg.gov.health.platform.accessmatrix;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** The matrix itself is visible: who may write where is a
 *  governance fact, not a code secret. */
@RestController @RequestMapping("/api/access-matrix")
public class AccessMatrixController {
  private final RolePermissionRepository repo;
  public AccessMatrixController(RolePermissionRepository repo) { this.repo = repo; }
  @GetMapping
  Map<String,Object> list() {
    if (TenantContext.get() == null) throw BizException.forbidden("يتطلب تسجيل الدخول");
    return Map.of("ok",true,"rules",repo.findAll(),
      "note","writes AND reads governed; longest prefix wins; a write path with no rule is refused for everyone but the admin tiers; oversight tiers (national/governorate/directorate) read without writing");
  }
}
