package eg.gov.health.platform.common;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BizException.class)
  ResponseEntity<Map<String,Object>> biz(BizException e, HttpServletRequest r) {
    return ResponseEntity.status(e.status()).body(Map.of("ok", false, "error", e.code(), "message", e.getMessage(),
      "correlation_id", String.valueOf(r.getAttribute("correlationId"))));
  }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e) {
    var f = e.getBindingResult().getFieldErrors().stream().findFirst();
    return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "validation_error",
      "message", f.map(x -> x.getField() + ": " + x.getDefaultMessage()).orElse("invalid request")));
  }
  @ExceptionHandler(NullPointerException.class)
  ResponseEntity<Map<String,Object>> npe(NullPointerException e) {
    // Facility-scoped endpoints assume a facility account; oversight
    // tiers (governorate / authority / national) have none, and for
    // them the honest answer is a clean scope refusal, not a 500.
    return ResponseEntity.status(403).body(Map.of("ok", false, "error", "facility_scope_required",
      "message", "هذا النطاق خاص بمنشأة محددة ويتطلب حساب منشأة — حسابات الإشراف تتابع من لوحات الإشراف والتقارير"));
  }
  @ExceptionHandler(Exception.class)
  ResponseEntity<Map<String,Object>> any(Exception e) {
    return ResponseEntity.status(500).body(Map.of("ok", false, "error", "internal_error", "message", String.valueOf(e.getMessage())));
  }
}
