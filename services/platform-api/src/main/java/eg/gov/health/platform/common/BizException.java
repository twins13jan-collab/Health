package eg.gov.health.platform.common;
import org.springframework.http.HttpStatus;
public class BizException extends RuntimeException {
  private final HttpStatus status; private final String code;
  public BizException(HttpStatus status, String code, String message) { super(message); this.status = status; this.code = code; }
  public HttpStatus status() { return status; } public String code() { return code; }
  public static BizException badRequest(String code, String msg) { return new BizException(HttpStatus.BAD_REQUEST, code, msg); }
  public static BizException notFound(String code, String msg) { return new BizException(HttpStatus.NOT_FOUND, code, msg); }
  public static BizException conflict(String code, String msg) { return new BizException(HttpStatus.CONFLICT, code, msg); }
  public static BizException forbidden(String msg) { return new BizException(HttpStatus.FORBIDDEN, "forbidden", msg); }
}
