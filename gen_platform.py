#!/usr/bin/env python3
"""Generate the Java/Spring Boot platform-api for the Egypt Health v6 rebuild."""
import pathlib, json

BASE = pathlib.Path.home() / "workspace/egypt-health-v6/services/platform-api"
PKG = BASE / "src/main/java/eg/gov/health/platform"
RES = BASE / "src/main/resources"
TEST = BASE / "src/test/java/eg/gov/health/platform"

def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")
    print("wrote", path.relative_to(BASE))

# ---------------------------------------------------------------- pom.xml
w(BASE / "pom.xml", """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
    <relativePath/>
  </parent>
  <groupId>eg.gov.health</groupId>
  <artifactId>platform-api</artifactId>
  <version>6.0.0</version>
  <name>Egypt National Health Platform API (Catalog v6 rebuild)</name>
  <properties>
    <java.version>21</java.version>
    <jjwt.version>0.12.6</jjwt.version>
    <springdoc.version>2.6.0</springdoc.version>
  </properties>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-actuator</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-database-postgresql</artifactId></dependency>
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>${jjwt.version}</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>${springdoc.version}</version></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
    </plugins>
  </build>
</project>""")

w(BASE / "Dockerfile", """FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/platform-api-*.jar app.jar
USER 1001
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]""")

w(RES / "application.yml", """spring:
  application: { name: egypt-health-platform-api }
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/egypt_health}
    username: ${SPRING_DATASOURCE_USERNAME:app_user}
    password: ${SPRING_DATASOURCE_PASSWORD:change-me}
  jpa:
    hibernate: { ddl-auto: validate }
    open-in-view: false
  flyway: { enabled: true, locations: classpath:db/migration }
server: { port: 8080 }
management: { endpoints: { web: { exposure: { include: health,info,metrics } } } }
app:
  jwt-secret: ${JWT_SECRET:dev-only-secret-change-me-dev-only-secret-change-me-0123456789}
  jwt-ttl-minutes: 60
  analytics-ai-base-url: ${ANALYTICS_AI_BASE_URL:http://localhost:8000}
springdoc: { swagger-ui: { path: /swagger-ui.html } }""")

# ---------------------------------------------------------------- common
w(PKG / "common/BizException.java", """package eg.gov.health.platform.common;
import org.springframework.http.HttpStatus;
public class BizException extends RuntimeException {
  private final HttpStatus status; private final String code;
  public BizException(HttpStatus status, String code, String message) { super(message); this.status = status; this.code = code; }
  public HttpStatus status() { return status; } public String code() { return code; }
  public static BizException badRequest(String code, String msg) { return new BizException(HttpStatus.BAD_REQUEST, code, msg); }
  public static BizException notFound(String code, String msg) { return new BizException(HttpStatus.NOT_FOUND, code, msg); }
  public static BizException conflict(String code, String msg) { return new BizException(HttpStatus.CONFLICT, code, msg); }
  public static BizException forbidden(String msg) { return new BizException(HttpStatus.FORBIDDEN, "forbidden", msg); }
}""")

w(PKG / "common/GlobalExceptionHandler.java", """package eg.gov.health.platform.common;
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
  @ExceptionHandler(Exception.class)
  ResponseEntity<Map<String,Object>> any(Exception e) {
    return ResponseEntity.status(500).body(Map.of("ok", false, "error", "internal_error", "message", String.valueOf(e.getMessage())));
  }
}""")

w(PKG / "common/TenantContext.java", """package eg.gov.health.platform.common;
/** Facility (tenant) scope for the current request, taken ONLY from the JWT —
 *  never from client-supplied ids (Catalog v6 rule). Consumed by the RLS
 *  DataSource wrapper to set app.current_facility_id per connection. */
public final class TenantContext {
  public record Principal(String userId, String email, String role, String facilityId, String governorate, String directorate) {}
  private static final ThreadLocal<Principal> CURRENT = new ThreadLocal<>();
  private TenantContext() {}
  public static void set(Principal p) { CURRENT.set(p); }
  public static Principal get() { return CURRENT.get(); }
  public static void clear() { CURRENT.remove(); }
  /** RLS setting value: facility uuid, 'gov:<governorate>', or 'national'. */
  public static String rlsSetting() {
    Principal p = CURRENT.get();
    if (p == null) return "";
    if ("national_admin".equals(p.role())) return "national";
    if (p.facilityId() != null && !p.facilityId().isBlank()) return p.facilityId();
    if (("governorate_admin".equals(p.role()) || "directorate_admin".equals(p.role())) && p.governorate() != null)
      return "gov:" + p.governorate();
    return "";
  }
}""")

w(PKG / "common/CorrelationFilter.java", """package eg.gov.health.platform.common;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component;
import java.io.IOException; import java.util.UUID;
@Component @Order(1)
public class CorrelationFilter extends OncePerRequestFilterShim {
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String cid = req.getHeader("X-Correlation-Id");
    if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
    req.setAttribute("correlationId", cid);
    res.setHeader("X-Correlation-Id", cid);
    try { chain.doFilter(req, res); } finally { TenantContext.clear(); }
  }
}""")
# fix: use real OncePerRequestFilter
p = PKG / "common/CorrelationFilter.java"
p.write_text(p.read_text().replace("OncePerRequestFilterShim", "org.springframework.web.filter.OncePerRequestFilter"), encoding="utf-8")

w(PKG / "common/AuditLog.java", """package eg.gov.health.platform.common;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "audit_logs")
public class AuditLog {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id") private UUID facilityId;
  private String governorate; private String actorId; private String actorRole;
  private String action; private String entityType; private String entityId;
  @Column(columnDefinition = "jsonb") private String payload;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AuditLog() {}
  public AuditLog(UUID facilityId, String governorate, String actorId, String actorRole, String action, String entityType, String entityId, String payload) {
    this.facilityId = facilityId; this.governorate = governorate; this.actorId = actorId; this.actorRole = actorRole;
    this.action = action; this.entityType = entityType; this.entityId = entityId; this.payload = payload;
  }
  public UUID getId() { return id; }
}""")

w(PKG / "common/AuditLogRepository.java", """package eg.gov.health.platform.common;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {}""")

w(PKG / "common/AuditService.java", """package eg.gov.health.platform.common;
import org.springframework.stereotype.Service; import java.util.UUID;
@Service
public class AuditService {
  private final AuditLogRepository repo;
  public AuditService(AuditLogRepository repo) { this.repo = repo; }
  public void record(String action, String entityType, String entityId, String payloadJson) {
    var p = TenantContext.get();
    UUID fac = null;
    try { if (p != null && p.facilityId() != null) fac = UUID.fromString(p.facilityId()); } catch (IllegalArgumentException ignored) {}
    repo.save(new AuditLog(fac, p == null ? null : p.governorate(), p == null ? null : p.userId(),
      p == null ? null : p.role(), action, entityType, entityId, payloadJson == null ? "{}" : payloadJson));
  }
}""")

# ---------------------------------------------------------------- security
w(PKG / "config/JwtService.java", """package eg.gov.health.platform.config;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.Instant; import java.util.*;
@Service
public class JwtService {
  private final SecretKey key; private final long ttlMinutes;
  public JwtService(@Value("${app.jwt-secret}") String secret, @Value("${app.jwt-ttl-minutes}") long ttlMinutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.ttlMinutes = ttlMinutes;
  }
  public String issue(String userId, String email, String role, String facilityId, String governorate, String directorate) {
    Instant now = Instant.now();
    return Jwts.builder().subject(userId).claim("email", email).claim("role", role)
      .claim("facility_id", facilityId).claim("governorate", governorate).claim("directorate", directorate)
      .id(UUID.randomUUID().toString()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(ttlMinutes * 60)))
      .signWith(key).compact();
  }
  public Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}""")

w(PKG / "config/SecurityConfig.java", """package eg.gov.health.platform.config;
import eg.gov.health.platform.common.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.List;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
  private final JwtService jwt;
  public SecurityConfig(JwtService jwt) { this.jwt = jwt; }
  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
    http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .requestMatchers("/fhir/**").authenticated()
        .anyRequest().authenticated())
      .addFilterBefore(new JwtFilter(jwt), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
  static class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt; JwtFilter(JwtService jwt) { this.jwt = jwt; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws ServletException, IOException {
      String h = req.getHeader("Authorization");
      if (h != null && h.startsWith("Bearer ")) {
        try {
          Claims c = jwt.parse(h.substring(7));
          TenantContext.set(new TenantContext.Principal(c.getSubject(), c.get("email", String.class),
            c.get("role", String.class), c.get("facility_id", String.class),
            c.get("governorate", String.class), c.get("directorate", String.class)));
          var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            c.getSubject(), null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + c.get("role", String.class))));
          org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) { /* unauthenticated: security chain will 401 protected routes */ }
      }
      chain.doFilter(req, res);
    }
  }
}""")

w(PKG / "PlatformApplication.java", """package eg.gov.health.platform;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class PlatformApplication {
  public static void main(String[] args) { SpringApplication.run(PlatformApplication.class, args); }
}""")

print("part 1 done")

# ---------------------------------------------------------------- identity
w(PKG / "identity/AppUser.java", """package eg.gov.health.platform.identity;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "app_users")
public class AppUser {
  @Id @GeneratedValue private UUID id;
  @Column(unique = true, nullable = false) private String email;
  @Column(name = "password_hash", nullable = false) private String passwordHash;
  @Column(nullable = false) private String role;
  @Column(name = "facility_id") private UUID facilityId;
  private String governorate; private String directorate;
  @Column(name = "full_name") private String fullName;
  @Column(name = "mfa_enabled") private boolean mfaEnabled;
  @Column(name = "is_active") private boolean active = true;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AppUser() {}
  public AppUser(String email, String passwordHash, String role, UUID facilityId, String governorate, String directorate, String fullName) {
    this.email = email; this.passwordHash = passwordHash; this.role = role; this.facilityId = facilityId;
    this.governorate = governorate; this.directorate = directorate; this.fullName = fullName;
  }
  public UUID getId() { return id; } public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; } public String getRole() { return role; }
  public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public String getDirectorate() { return directorate; } public String getFullName() { return fullName; }
  public boolean isActive() { return active; } public boolean isMfaEnabled() { return mfaEnabled; }
}""")

w(PKG / "identity/AppUserRepository.java", """package eg.gov.health.platform.identity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
  Optional<AppUser> findByEmail(String email);
}""")

w(PKG / "identity/AuthController.java", """package eg.gov.health.platform.identity;
import eg.gov.health.platform.common.*; import eg.gov.health.platform.config.JwtService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final AppUserRepository users; private final PasswordEncoder encoder; private final JwtService jwt; private final AuditService audit;
  public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwt, AuditService audit) {
    this.users = users; this.encoder = encoder; this.jwt = jwt; this.audit = audit;
  }
  public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
  @PostMapping("/login")
  Map<String, Object> login(@RequestBody @jakarta.validation.Valid LoginRequest r) {
    var user = users.findByEmail(r.email())
      .orElseThrow(() -> BizException.badRequest("invalid_credentials", "بيانات الدخول غير صحيحة"));
    if (!user.isActive() || !encoder.matches(r.password(), user.getPasswordHash()))
      throw BizException.badRequest("invalid_credentials", "بيانات الدخول غير صحيحة");
    String token = jwt.issue(user.getId().toString(), user.getEmail(), user.getRole(),
      user.getFacilityId() == null ? null : user.getFacilityId().toString(), user.getGovernorate(), user.getDirectorate());
    return Map.of("ok", true, "access_token", token, "token_type", "Bearer",
      "user", Map.of("id", user.getId(), "email", user.getEmail(), "role", user.getRole(),
        "facility_id", user.getFacilityId() == null ? "" : user.getFacilityId(), "full_name", user.getFullName() == null ? "" : user.getFullName()));
  }
}""")

# ---------------------------------------------------------------- facility
w(PKG / "facility/Facility.java", """package eg.gov.health.platform.facility;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "facilities")
public class Facility {
  @Id @GeneratedValue private UUID id;
  @Column(name = "name_ar", nullable = false) private String nameAr;
  @Column(name = "name_en") private String nameEn;
  @Column(nullable = false) private String type;   // hospital, health_unit, family_center, ...
  @Column(nullable = false) private String level;  // primary, secondary, tertiary, quaternary
  @Column(nullable = false) private String governorate;
  private String directorate; private String city;
  @Column(name = "ownership") private String ownership; // moh, university, insurance, military, police, private, ngo, sectoral
  @Column(name = "bed_capacity") private Integer bedCapacity;
  @Column(nullable = false) private String status = "active";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Facility() {}
  public Facility(String nameAr, String nameEn, String type, String level, String governorate, String directorate, String city, String ownership, Integer bedCapacity) {
    this.nameAr = nameAr; this.nameEn = nameEn; this.type = type; this.level = level; this.governorate = governorate;
    this.directorate = directorate; this.city = city; this.ownership = ownership; this.bedCapacity = bedCapacity;
  }
  public UUID getId() { return id; } public String getNameAr() { return nameAr; } public String getNameEn() { return nameEn; }
  public String getType() { return type; } public String getLevel() { return level; } public String getGovernorate() { return governorate; }
  public String getDirectorate() { return directorate; } public String getCity() { return city; }
  public String getOwnership() { return ownership; } public Integer getBedCapacity() { return bedCapacity; } public String getStatus() { return status; }
}""")

w(PKG / "facility/FacilityRepository.java", """package eg.gov.health.platform.facility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface FacilityRepository extends JpaRepository<Facility, UUID> {
  List<Facility> findByGovernorate(String governorate);
}""")

w(PKG / "facility/FacilityController.java", """package eg.gov.health.platform.facility;
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
}""")

# ---------------------------------------------------------------- patient MPI
w(PKG / "patient/Patient.java", """package eg.gov.health.platform.patient;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "patients")
public class Patient {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id") private UUID facilityId;
  private String governorate;
  @Column(name = "national_id", unique = true) private String nationalId;
  @Column(name = "full_name", nullable = false) private String fullName;
  @Column(name = "birth_date") private LocalDate birthDate;
  private String gender; private String phone; private String address;
  @Column(name = "identity_status", nullable = false) private String identityStatus = "verified"; // verified, temporary, unknown
  @Column(name = "merged_into") private UUID mergedInto;
  @Column(nullable = false) private String status = "active";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Patient() {}
  public Patient(UUID facilityId, String governorate, String nationalId, String fullName, LocalDate birthDate, String gender, String phone, String address, String identityStatus) {
    this.facilityId = facilityId; this.governorate = governorate; this.nationalId = nationalId; this.fullName = fullName;
    this.birthDate = birthDate; this.gender = gender; this.phone = phone; this.address = address;
    this.identityStatus = identityStatus == null ? "verified" : identityStatus;
  }
  public UUID getId() { return id; } public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public String getNationalId() { return nationalId; } public String getFullName() { return fullName; }
  public LocalDate getBirthDate() { return birthDate; } public String getGender() { return gender; }
  public String getPhone() { return phone; } public String getAddress() { return address; }
  public String getIdentityStatus() { return identityStatus; } public UUID getMergedInto() { return mergedInto; }
  public String getStatus() { return status; }
  public void markMergedInto(UUID target) { this.mergedInto = target; this.status = "merged"; this.updatedAt = OffsetDateTime.now(); }
}""")

w(PKG / "patient/PatientRepository.java", """package eg.gov.health.platform.patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate; import java.util.*; import java.util.UUID;
public interface PatientRepository extends JpaRepository<Patient, UUID> {
  Optional<Patient> findByNationalId(String nationalId);
  List<Patient> findByFullNameAndBirthDate(String fullName, LocalDate birthDate);
  List<Patient> findByFacilityId(UUID facilityId);
}""")

w(PKG / "patient/DuplicateDetector.java", """package eg.gov.health.platform.patient;
import java.time.LocalDate;
/** MPI duplicate rules (Catalog cycle 217): NEVER auto-merge on name alone.
 *  A candidate is raised only with explainable evidence; a human identity
 *  officer decides. National-id equality is a hard duplicate (rejected at
 *  registration), name+birthdate or phone+birthdate raise a review case. */
public final class DuplicateDetector {
  private DuplicateDetector() {}
  public record Candidate(String reason, int score) {}
  public static Candidate evaluate(String nameA, LocalDate dobA, String phoneA, String nameB, LocalDate dobB, String phoneB) {
    int score = 0; var reasons = new java.util.ArrayList<String>();
    if (nameA != null && nameA.trim().equalsIgnoreCase(nameB == null ? "" : nameB.trim())) { score += 40; reasons.add("same_name"); }
    if (dobA != null && dobA.equals(dobB)) { score += 40; reasons.add("same_birth_date"); }
    if (phoneA != null && !phoneA.isBlank() && phoneA.equals(phoneB)) { score += 30; reasons.add("same_phone"); }
    return new Candidate(String.join("+", reasons), score);
  }
  public static boolean isReviewCandidate(Candidate c) { return c.score() >= 70; }
}""")

w(PKG / "patient/PatientController.java", """package eg.gov.health.platform.patient;
import eg.gov.health.platform.common.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/patients")
public class PatientController {
  private final PatientRepository repo; private final AuditService audit;
  public PatientController(PatientRepository repo, AuditService audit) { this.repo = repo; this.audit = audit; }
  public record PatientRequest(String nationalId, @NotBlank String fullName, LocalDate birthDate, String gender,
                               String phone, String address, String identityStatus) {}
  @PostMapping
  Map<String, Object> register(@RequestBody @jakarta.validation.Valid PatientRequest r) {
    var p = TenantContext.get();
    UUID fac = p != null && p.facilityId() != null ? UUID.fromString(p.facilityId()) : null;
    if (r.nationalId() != null && !r.nationalId().isBlank()) {
      var existing = repo.findByNationalId(r.nationalId());
      if (existing.isPresent())
        throw BizException.conflict("duplicate_national_id", "رقم قومي مسجل مسبقًا — لا يُنشأ معرف مكرر (دورة 217)");
    }
    var saved = repo.save(new Patient(fac, p == null ? null : p.governorate(), r.nationalId(), r.fullName(),
      r.birthDate(), r.gender(), r.phone(), r.address(), r.identityStatus()));
    // Explainable duplicate candidates for human review (never auto-merge).
    var candidates = new java.util.ArrayList<Map<String, Object>>();
    if (r.birthDate() != null) {
      for (var other : repo.findByFullNameAndBirthDate(r.fullName(), r.birthDate())) {
        if (other.getId().equals(saved.getId())) continue;
        var c = DuplicateDetector.evaluate(r.fullName(), r.birthDate(), r.phone(), other.getFullName(), other.getBirthDate(), other.getPhone());
        if (DuplicateDetector.isReviewCandidate(c))
          candidates.add(Map.of("patient_id", other.getId(), "reason", c.reason(), "score", c.score()));
      }
    }
    audit.record("patient.registered", "patient", saved.getId().toString(), "{}");
    return Map.of("ok", true, "patient", saved, "duplicate_candidates", candidates);
  }
  @GetMapping("/{id}")
  Patient get(@PathVariable UUID id) {
    return repo.findById(id).orElseThrow(() -> BizException.notFound("patient_not_found", "المريض غير موجود"));
  }
  @GetMapping
  Map<String, Object> list() {
    var p = TenantContext.get();
    var rows = (p != null && p.facilityId() != null) ? repo.findByFacilityId(UUID.fromString(p.facilityId())) : repo.findAll();
    return Map.of("ok", true, "patients", rows, "count", rows.size());
  }
  /** Merge (cycle 217): identity-officer only, fully audited, reversible via
   *  the preserved merged_into link; source record is never deleted. */
  @PostMapping("/{sourceId}/merge-into/{targetId}")
  @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String, Object> merge(@PathVariable UUID sourceId, @PathVariable UUID targetId, @RequestBody Map<String, String> body) {
    if (sourceId.equals(targetId)) throw BizException.badRequest("same_patient", "لا يمكن دمج المريض في نفسه");
    if (body.getOrDefault("reason", "").isBlank()) throw BizException.badRequest("reason_required", "سبب الدمج مطلوب وموثق");
    var source = get(sourceId); var target = get(targetId);
    if (!"active".equals(source.getStatus())) throw BizException.conflict("already_merged", "المصدر مدمج مسبقًا");
    source.markMergedInto(targetId); repo.save(source);
    audit.record("patient.merged", "patient", sourceId.toString(),
      "{\\"target\\":\\"" + targetId + "\\",\\"reason\\":\\"" + body.get("reason").replace("\\"", "'") + "\\"}");
    return Map.of("ok", true, "source", sourceId, "target", targetId, "status", "merged");
  }
}""")
print("part 2 done")

# ---------------------------------------------------------------- clinical
w(PKG / "clinical/News2Calculator.java", """package eg.gov.health.platform.clinical;
/** NEWS2 (RCP 2017) adult early-warning score — pure, fully tested.
 *  Advisory only: the score supports, never replaces, clinical judgement. */
public final class News2Calculator {
  private News2Calculator() {}
  public record Vitals(int respiratoryRate, int spo2, boolean onOxygen, int systolicBp, int pulse, double temperature, String consciousness) {}
  public record Score(int total, int rr, int spo2, int bp, int pulse, int temp, int consciousness, String riskBand) {}
  public static Score score(Vitals v) {
    int rr = v.respiratoryRate() <= 8 ? 3 : v.respiratoryRate() <= 11 ? 1 : v.respiratoryRate() <= 20 ? 0 : v.respiratoryRate() <= 24 ? 2 : 3;
    int spo2 = v.spo2() <= 91 ? 3 : v.spo2() <= 93 ? 2 : v.spo2() <= 95 ? 1 : 0;
    if (v.onOxygen()) spo2 += 2;
    int bp = v.systolicBp() <= 90 ? 3 : v.systolicBp() <= 100 ? 2 : v.systolicBp() <= 110 ? 1 : v.systolicBp() <= 219 ? 0 : 3;
    int pulse = v.pulse() <= 40 ? 3 : v.pulse() <= 50 ? 1 : v.pulse() <= 90 ? 0 : v.pulse() <= 110 ? 1 : v.pulse() <= 130 ? 2 : 3;
    int temp = v.temperature() <= 35.0 ? 3 : v.temperature() <= 36.0 ? 1 : v.temperature() <= 38.0 ? 0 : v.temperature() <= 39.0 ? 1 : 2;
    int cons = (v.consciousness() == null || v.consciousness().equalsIgnoreCase("alert")) ? 0 : 3;
    int total = rr + spo2 + bp + pulse + temp + cons;
    boolean anyThree = rr == 3 || spo2 >= 3 || bp == 3 || pulse == 3 || temp == 3 || cons == 3;
    String band = total >= 7 ? "high" : total >= 5 ? "medium" : (anyThree ? "low-medium" : "low");
    return new Score(total, rr, spo2, bp, pulse, temp, cons, band);
  }
}""")

w(PKG / "clinical/Encounter.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "encounters")
public class Encounter {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  @Column(nullable = false) private String type = "outpatient"; // emergency, outpatient, inpatient, telemedicine, home
  @Column(name = "chief_complaint") private String chiefComplaint;
  @Column(name = "triage_level") private Integer triageLevel; // 1..5
  @Column(name = "news2_score") private Integer news2Score;
  @Column(nullable = false) private String status = "open"; // open, in_progress, completed, closed, cancelled
  @Column(name = "arrived_at") private OffsetDateTime arrivedAt = OffsetDateTime.now();
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Encounter() {}
  public Encounter(UUID facilityId, String governorate, UUID patientId, String type, String chiefComplaint, Integer triageLevel, Integer news2Score) {
    this.facilityId = facilityId; this.governorate = governorate; this.patientId = patientId;
    this.type = type == null ? "outpatient" : type; this.chiefComplaint = chiefComplaint;
    this.triageLevel = triageLevel; this.news2Score = news2Score;
  }
  public UUID getId() { return id; } public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public UUID getPatientId() { return patientId; } public String getType() { return type; }
  public String getChiefComplaint() { return chiefComplaint; } public Integer getTriageLevel() { return triageLevel; }
  public Integer getNews2Score() { return news2Score; } public String getStatus() { return status; }
  public OffsetDateTime getArrivedAt() { return arrivedAt; }
  public void setStatus(String s) { this.status = s; this.updatedAt = OffsetDateTime.now(); }
}""")

w(PKG / "clinical/EncounterRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface EncounterRepository extends JpaRepository<Encounter, UUID> {
  List<Encounter> findByFacilityIdOrderByArrivedAtDesc(UUID facilityId);
  List<Encounter> findByPatientIdOrderByArrivedAtDesc(UUID patientId);
}""")

w(PKG / "clinical/EncounterController.java", """package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/encounters")
public class EncounterController {
  private final EncounterRepository repo; private final AuditService audit;
  public EncounterController(EncounterRepository repo, AuditService audit) { this.repo = repo; this.audit = audit; }
  public record VitalsDto(Integer respiratoryRate, Integer spo2, Boolean onOxygen, Integer systolicBp, Integer pulse, Double temperature, String consciousness) {}
  public record EncounterRequest(UUID patientId, String type, String chiefComplaint, Integer triageLevel, VitalsDto vitals) {}
  @PostMapping
  Map<String, Object> create(@RequestBody EncounterRequest r) {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة لإنشاء زيارة");
    if (r.patientId() == null) throw BizException.badRequest("missing_field", "patientId مطلوب");
    Integer news2 = null; News2Calculator.Score score = null;
    if (r.vitals() != null && r.vitals().respiratoryRate() != null) {
      var v = r.vitals();
      score = News2Calculator.score(new News2Calculator.Vitals(v.respiratoryRate(), v.spo2() == null ? 98 : v.spo2(),
        Boolean.TRUE.equals(v.onOxygen()), v.systolicBp() == null ? 120 : v.systolicBp(), v.pulse() == null ? 80 : v.pulse(),
        v.temperature() == null ? 37.0 : v.temperature(), v.consciousness()));
      news2 = score.total();
    }
    var e = repo.save(new Encounter(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.type(), r.chiefComplaint(), r.triageLevel(), news2));
    audit.record("encounter.created", "encounter", e.getId().toString(), "{}");
    var out = new java.util.HashMap<String, Object>(); out.put("ok", true); out.put("encounter", e);
    if (score != null) { out.put("news2", score); out.put("advisory", "NEWS2 دعم قرار فقط — القرار السريري للطبيب المعالج"); }
    return out;
  }
  @GetMapping
  Map<String, Object> list(@RequestParam(required = false) UUID patientId) {
    var p = TenantContext.get();
    var rows = patientId != null ? repo.findByPatientIdOrderByArrivedAtDesc(patientId)
      : repo.findByFacilityIdOrderByArrivedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok", true, "encounters", rows, "count", rows.size());
  }
  @GetMapping("/{id}")
  Encounter get(@PathVariable UUID id) { return repo.findById(id).orElseThrow(() -> BizException.notFound("encounter_not_found", "الزيارة غير موجودة")); }
  @PostMapping("/{id}/status")
  Map<String, Object> status(@PathVariable UUID id, @RequestBody Map<String, String> body) {
    var e = get(id); String s = body.getOrDefault("status", "");
    if (!List.of("open", "in_progress", "completed", "closed", "cancelled").contains(s))
      throw BizException.badRequest("invalid_status", "حالة غير مسموحة");
    e.setStatus(s); repo.save(e);
    audit.record("encounter.status." + s, "encounter", id.toString(), "{}");
    return Map.of("ok", true, "encounter", e);
  }
}""")

# ---------------------------------------------------------------- cycles
w(PKG / "cycles/CycleStateMachine.java", """package eg.gov.health.platform.cycles;
import java.util.*;
/** The unified operating-cycle state machine of Catalog v6.
 *  Rules: only listed transitions are legal; terminal states never leave;
 *  cancellation / failure / correction / reconciliation / hold REQUIRE a
 *  documented reason; pending_review -> completed is the review gate
 *  (a mandatory step that cannot be skipped from in_progress). */
public final class CycleStateMachine {
  private CycleStateMachine() {}
  public static final List<String> STATUSES = List.of("draft","open","in_progress","on_hold","pending_review",
    "completed","closed","cancelled","failed","corrected","reconciled");
  public static final Set<String> TERMINAL = Set.of("closed","cancelled");
  public static final Map<String, List<String>> TRANSITIONS = Map.ofEntries(
    Map.entry("draft", List.of("open")),
    Map.entry("open", List.of("in_progress","cancelled")),
    Map.entry("in_progress", List.of("on_hold","pending_review","failed")),
    Map.entry("on_hold", List.of("in_progress")),
    Map.entry("pending_review", List.of("in_progress","completed")),
    Map.entry("completed", List.of("closed","corrected")),
    Map.entry("corrected", List.of("closed")),
    Map.entry("failed", List.of("reconciled")),
    Map.entry("reconciled", List.of("closed")),
    Map.entry("cancelled", List.of("reconciled")),
    Map.entry("closed", List.of()));
  public static final Set<String> REASON_REQUIRED = Set.of("cancelled","failed","corrected","reconciled","on_hold");
  public static boolean isLegal(String from, String to) {
    return TRANSITIONS.getOrDefault(from, List.of()).contains(to);
  }
}""")

w(PKG / "cycles/CycleDefinition.java", """package eg.gov.health.platform.cycles;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name = "cycle_definitions")
public class CycleDefinition {
  @Id @GeneratedValue private UUID id;
  @Column(unique = true, nullable = false) private String code;
  @Column(name = "catalog_number", unique = true, nullable = false) private int catalogNumber;
  @Column(name = "title_ar", nullable = false) private String titleAr;
  @Column(nullable = false) private String domain;
  @Column(name = "specialised_route") private String specialisedRoute;
  @Column(name = "sla_hours", nullable = false) private int slaHours = 72;
  @Column(nullable = false) private String status = "active";
  protected CycleDefinition() {}
  public String getCode() { return code; } public int getCatalogNumber() { return catalogNumber; }
  public String getTitleAr() { return titleAr; } public String getDomain() { return domain; }
  public String getSpecialisedRoute() { return specialisedRoute; } public int getSlaHours() { return slaHours; }
  public String getStatus() { return status; } public UUID getId() { return id; }
}""")

w(PKG / "cycles/CycleInstance.java", """package eg.gov.health.platform.cycles;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "cycle_instances")
public class CycleInstance {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "definition_code", nullable = false) private String definitionCode;
  @Column(name = "subject_type") private String subjectType;
  @Column(name = "subject_ref") private String subjectRef;
  @Column(name = "patient_id") private UUID patientId;
  @Column(nullable = false) private String title;
  @Column(name = "current_step") private String currentStep;
  @Column(name = "protocol_version") private String protocolVersion;
  @Column(name = "correlation_id", nullable = false) private String correlationId;
  @Column(name = "idempotency_key") private String idempotencyKey;
  @Column(name = "sla_due_at") private OffsetDateTime slaDueAt;
  private boolean escalated;
  @Column(nullable = false) private String status = "draft";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CycleInstance() {}
  public CycleInstance(UUID facilityId, String governorate, String definitionCode, String subjectType, String subjectRef,
                       UUID patientId, String title, String protocolVersion, String correlationId, String idempotencyKey, OffsetDateTime slaDueAt) {
    this.facilityId = facilityId; this.governorate = governorate; this.definitionCode = definitionCode;
    this.subjectType = subjectType; this.subjectRef = subjectRef; this.patientId = patientId; this.title = title;
    this.protocolVersion = protocolVersion; this.correlationId = correlationId; this.idempotencyKey = idempotencyKey; this.slaDueAt = slaDueAt;
  }
  public UUID getId() { return id; } public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public String getDefinitionCode() { return definitionCode; } public String getTitle() { return title; }
  public String getStatus() { return status; } public String getCorrelationId() { return correlationId; }
  public String getIdempotencyKey() { return idempotencyKey; } public OffsetDateTime getSlaDueAt() { return slaDueAt; }
  public boolean isEscalated() { return escalated; } public OffsetDateTime getCreatedAt() { return createdAt; }
  public String getSubjectType() { return subjectType; } public String getSubjectRef() { return subjectRef; }
  public UUID getPatientId() { return patientId; } public String getCurrentStep() { return currentStep; }
  public String getProtocolVersion() { return protocolVersion; }
  public void transitionTo(String to, String step) { this.status = to; if (step != null) this.currentStep = step; this.updatedAt = OffsetDateTime.now(); }
  public void escalate() { this.escalated = true; this.updatedAt = OffsetDateTime.now(); }
}""")

w(PKG / "cycles/CycleEvent.java", """package eg.gov.health.platform.cycles;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "cycle_events")
public class CycleEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "instance_id", nullable = false) private UUID instanceId;
  @Column(name = "event_type", nullable = false) private String eventType;
  @Column(name = "from_status") private String fromStatus;
  @Column(name = "to_status") private String toStatus;
  @Column(name = "actor_id") private String actorId;
  @Column(name = "actor_role") private String actorRole;
  private String reason;
  @Column(nullable = false) private String status = "recorded";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CycleEvent() {}
  public CycleEvent(UUID facilityId, String governorate, UUID instanceId, String eventType, String fromStatus, String toStatus, String actorId, String actorRole, String reason) {
    this.facilityId = facilityId; this.governorate = governorate; this.instanceId = instanceId; this.eventType = eventType;
    this.fromStatus = fromStatus; this.toStatus = toStatus; this.actorId = actorId; this.actorRole = actorRole; this.reason = reason;
  }
  public UUID getId() { return id; } public UUID getInstanceId() { return instanceId; }
  public String getEventType() { return eventType; } public String getFromStatus() { return fromStatus; }
  public String getToStatus() { return toStatus; } public String getReason() { return reason; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}""")

w(PKG / "cycles/CycleRepositories.java", """package eg.gov.health.platform.cycles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
interface CycleDefinitionRepository extends JpaRepository<CycleDefinition, UUID> {
  Optional<CycleDefinition> findByCode(String code);
  List<CycleDefinition> findByStatusOrderByCatalogNumber(String status);
}
interface CycleInstanceRepository extends JpaRepository<CycleInstance, UUID> {
  List<CycleInstance> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  Optional<CycleInstance> findByFacilityIdAndIdempotencyKey(UUID facilityId, String idempotencyKey);
  long countByFacilityIdAndStatus(UUID facilityId, String status);
}
interface CycleEventRepository extends JpaRepository<CycleEvent, UUID> {
  List<CycleEvent> findByInstanceIdOrderByCreatedAtAsc(UUID instanceId);
}""")
# repositories must be public for Spring — rewrite as separate public files below in part 4
print("part 3 done")

# fix repositories into public single files
(PKG / "cycles/CycleRepositories.java").unlink(missing_ok=True)
w(PKG / "cycles/CycleDefinitionRepository.java", """package eg.gov.health.platform.cycles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface CycleDefinitionRepository extends JpaRepository<CycleDefinition, UUID> {
  Optional<CycleDefinition> findByCode(String code);
  List<CycleDefinition> findByStatusOrderByCatalogNumber(String status);
}""")
w(PKG / "cycles/CycleInstanceRepository.java", """package eg.gov.health.platform.cycles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface CycleInstanceRepository extends JpaRepository<CycleInstance, UUID> {
  List<CycleInstance> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  Optional<CycleInstance> findByFacilityIdAndIdempotencyKey(UUID facilityId, String idempotencyKey);
  long countByFacilityIdAndStatus(UUID facilityId, String status);
  List<CycleInstance> findByFacilityIdAndSlaDueAtBeforeAndStatusNotIn(UUID facilityId, java.time.OffsetDateTime when, List<String> statuses);
}""")
w(PKG / "cycles/CycleEventRepository.java", """package eg.gov.health.platform.cycles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface CycleEventRepository extends JpaRepository<CycleEvent, UUID> {
  List<CycleEvent> findByInstanceIdOrderByCreatedAtAsc(UUID instanceId);
}""")

w(PKG / "cycles/CycleController.java", """package eg.gov.health.platform.cycles;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/cycle-engine")
public class CycleController {
  private final CycleDefinitionRepository defs; private final CycleInstanceRepository instances;
  private final CycleEventRepository events; private final AuditService audit;
  public CycleController(CycleDefinitionRepository defs, CycleInstanceRepository instances, CycleEventRepository events, AuditService audit) {
    this.defs = defs; this.instances = instances; this.events = events; this.audit = audit;
  }
  private UUID facility() {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة لتشغيل دورات العمل");
    return UUID.fromString(p.facilityId());
  }
  @GetMapping("/definitions")
  Map<String, Object> definitions() {
    var rows = defs.findByStatusOrderByCatalogNumber("active");
    return Map.of("ok", true, "definitions", rows, "count", rows.size());
  }
  @GetMapping("/transitions")
  Map<String, Object> transitions() {
    return Map.of("ok", true, "transitions", CycleStateMachine.TRANSITIONS, "statuses", CycleStateMachine.STATUSES,
      "terminal", CycleStateMachine.TERMINAL, "reason_required", CycleStateMachine.REASON_REQUIRED);
  }
  public record CreateInstance(String definitionCode, String title, String subjectType, String subjectRef,
                               UUID patientId, String protocolVersion, String correlationId, String idempotencyKey, Integer slaHours) {}
  @PostMapping("/instances") @Transactional
  Map<String, Object> create(@RequestBody CreateInstance r) {
    UUID fac = facility(); var p = TenantContext.get();
    if (r.definitionCode() == null || r.title() == null) throw BizException.badRequest("missing_field", "definitionCode و title مطلوبان");
    var def = defs.findByCode(r.definitionCode()).orElseThrow(() -> BizException.badRequest("unknown_definition", "تعريف دورة غير معروف"));
    if (r.idempotencyKey() != null) {
      var dup = instances.findByFacilityIdAndIdempotencyKey(fac, r.idempotencyKey());
      if (dup.isPresent()) return Map.of("ok", true, "replayed", true, "instance", dup.get());
    }
    int sla = r.slaHours() != null ? r.slaHours() : def.getSlaHours();
    var inst = instances.save(new CycleInstance(fac, p.governorate(), r.definitionCode(), r.subjectType(), r.subjectRef(),
      r.patientId(), r.title(), r.protocolVersion(), r.correlationId() != null ? r.correlationId() : UUID.randomUUID().toString(),
      r.idempotencyKey(), OffsetDateTime.now().plusHours(sla)));
    events.save(new CycleEvent(fac, p.governorate(), inst.getId(), "created", null, "draft", p.userId(), p.role(), null));
    audit.record("cycle.created", "cycle_instance", inst.getId().toString(), "{}");
    return Map.of("ok", true, "instance", inst);
  }
  @GetMapping("/instances")
  Map<String, Object> list() {
    var rows = instances.findByFacilityIdOrderByCreatedAtDesc(facility());
    return Map.of("ok", true, "instances", rows, "count", rows.size());
  }
  @GetMapping("/instances/{id}")
  CycleInstance get(@PathVariable UUID id) {
    var inst = instances.findById(id).orElseThrow(() -> BizException.notFound("instance_not_found", "الدورة غير موجودة"));
    if (!inst.getFacilityId().equals(facility())) throw BizException.notFound("instance_not_found", "الدورة غير موجودة");
    return inst;
  }
  @GetMapping("/instances/{id}/events")
  Map<String, Object> instanceEvents(@PathVariable UUID id) {
    get(id); var rows = events.findByInstanceIdOrderByCreatedAtAsc(id);
    return Map.of("ok", true, "events", rows, "count", rows.size());
  }
  public record TransitionRequest(String toStatus, String reason, String currentStep) {}
  @PostMapping("/instances/{id}/transition") @Transactional
  Map<String, Object> transition(@PathVariable UUID id, @RequestBody TransitionRequest r) {
    var p = TenantContext.get(); var inst = get(id);
    String to = r.toStatus();
    if (to == null || !CycleStateMachine.STATUSES.contains(to)) throw BizException.badRequest("invalid_status", "حالة غير معروفة");
    if (CycleStateMachine.REASON_REQUIRED.contains(to) && (r.reason() == null || r.reason().isBlank()))
      throw BizException.badRequest("reason_required", "هذا الانتقال يتطلب توثيق السبب (كتالوج v6)");
    if (!CycleStateMachine.isLegal(inst.getStatus(), to))
      throw BizException.conflict("illegal_transition", "انتقال غير مسموح: " + inst.getStatus() + " -> " + to
        + " | المسموح: " + CycleStateMachine.TRANSITIONS.getOrDefault(inst.getStatus(), List.of()));
    String from = inst.getStatus();
    inst.transitionTo(to, r.currentStep()); instances.save(inst);
    events.save(new CycleEvent(inst.getFacilityId(), p == null ? null : p.governorate(), id, "transition", from, to,
      p == null ? null : p.userId(), p == null ? null : p.role(), r.reason()));
    audit.record("cycle.transition." + to, "cycle_instance", id.toString(), "{}");
    return Map.of("ok", true, "instance", inst);
  }
  @PostMapping("/instances/{id}/escalate") @Transactional
  Map<String, Object> escalate(@PathVariable UUID id, @RequestBody Map<String, String> body) {
    var p = TenantContext.get(); var inst = get(id);
    if (body.getOrDefault("reason", "").isBlank()) throw BizException.badRequest("reason_required", "سبب التصعيد مطلوب");
    if (CycleStateMachine.TERMINAL.contains(inst.getStatus())) throw BizException.conflict("terminal_instance", "دورة منتهية لا تُصعَّد");
    inst.escalate(); instances.save(inst);
    events.save(new CycleEvent(inst.getFacilityId(), p == null ? null : p.governorate(), id, "escalated", inst.getStatus(), inst.getStatus(),
      p == null ? null : p.userId(), p == null ? null : p.role(), body.get("reason")));
    return Map.of("ok", true, "instance", inst);
  }
  @GetMapping("/summary")
  Map<String, Object> summary() {
    UUID fac = facility(); var byStatus = new java.util.LinkedHashMap<String, Long>();
    for (String s : CycleStateMachine.STATUSES) byStatus.put(s, instances.countByFacilityIdAndStatus(fac, s));
    var breaches = instances.findByFacilityIdAndSlaDueAtBeforeAndStatusNotIn(fac, OffsetDateTime.now(), List.of("closed", "cancelled", "completed"));
    return Map.of("ok", true, "summary", Map.of("by_status", byStatus, "sla_breaches", breaches.size()));
  }
  @GetMapping("/sla-breaches")
  Map<String, Object> slaBreaches() {
    var rows = instances.findByFacilityIdAndSlaDueAtBeforeAndStatusNotIn(facility(), OffsetDateTime.now(), List.of("closed", "cancelled", "completed"));
    return Map.of("ok", true, "sla_breaches", rows, "count", rows.size());
  }
}""")

# ---------------------------------------------------------------- FHIR
w(PKG / "fhir/FhirController.java", """package eg.gov.health.platform.fhir;
import eg.gov.health.platform.clinical.*; import eg.gov.health.platform.common.BizException; import eg.gov.health.platform.patient.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** HL7 FHIR R4 exchange surface (Catalog: FHIR for modern exchange).
 *  Maps the internal domain to FHIR resources; internal ids stay stable so
 *  links survive identity merges (merged patients return the target link). */
@RestController @RequestMapping("/fhir")
public class FhirController {
  private final PatientRepository patients; private final EncounterRepository encounters;
  public FhirController(PatientRepository patients, EncounterRepository encounters) { this.patients = patients; this.encounters = encounters; }
  public static Map<String, Object> toFhirPatient(Patient p) {
    var m = new LinkedHashMap<String, Object>();
    m.put("resourceType", "Patient"); m.put("id", p.getId().toString()); m.put("active", "active".equals(p.getStatus()));
    if (p.getNationalId() != null) m.put("identifier", List.of(Map.of("system", "https://health.gov.eg/national-id", "value", p.getNationalId())));
    m.put("name", List.of(Map.of("text", p.getFullName())));
    if (p.getGender() != null) m.put("gender", p.getGender());
    if (p.getBirthDate() != null) m.put("birthDate", p.getBirthDate().toString());
    if (p.getMergedInto() != null) m.put("link", List.of(Map.of("other", Map.of("reference", "Patient/" + p.getMergedInto()), "type", "replaced-by")));
    return m;
  }
  public static Map<String, Object> toFhirEncounter(Encounter e) {
    var m = new LinkedHashMap<String, Object>();
    m.put("resourceType", "Encounter"); m.put("id", e.getId().toString());
    m.put("status", "closed".equals(e.getStatus()) || "completed".equals(e.getStatus()) ? "finished" : "in-progress");
    m.put("class", Map.of("code", "emergency".equals(e.getType()) ? "EMER" : "AMB"));
    m.put("subject", Map.of("reference", "Patient/" + e.getPatientId()));
    m.put("period", Map.of("start", e.getArrivedAt().toString()));
    if (e.getChiefComplaint() != null) m.put("reasonCode", List.of(Map.of("text", e.getChiefComplaint())));
    return m;
  }
  @GetMapping("/Patient/{id}")
  Map<String, Object> patient(@PathVariable UUID id) {
    return toFhirPatient(patients.findById(id).orElseThrow(() -> BizException.notFound("patient_not_found", "Patient not found")));
  }
  @GetMapping("/Encounter/{id}")
  Map<String, Object> encounter(@PathVariable UUID id) {
    return toFhirEncounter(encounters.findById(id).orElseThrow(() -> BizException.notFound("encounter_not_found", "Encounter not found")));
  }
}""")

# ---------------------------------------------------------------- integration with analytics-ai
w(PKG / "integration/AnalyticsClient.java", """package eg.gov.health.platform.integration;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.MediaType;
import org.springframework.stereotype.Service; import org.springframework.web.client.RestClient;
import java.util.Map;
/** Calls the Python analytics/AI service. Its outputs are ADVISORY only:
 *  the core never executes a clinical or financial decision from them
 *  without a human actor (Catalog v6 guardrail). Correlation ids propagate. */
@Service
public class AnalyticsClient {
  private final RestClient http;
  public AnalyticsClient(@Value("${app.analytics-ai-base-url}") String baseUrl) {
    this.http = RestClient.builder().baseUrl(baseUrl).build();
  }
  @SuppressWarnings("unchecked")
  public Map<String, Object> triageSupport(Map<String, Object> vitals, String correlationId) {
    return http.post().uri("/v1/ai/triage-support")
      .header("X-Correlation-Id", correlationId == null ? "" : correlationId)
      .contentType(MediaType.APPLICATION_JSON).body(vitals)
      .retrieve().body(Map.class);
  }
  @SuppressWarnings("unchecked")
  public Map<String, Object> capacityForecast(Map<String, Object> series, String correlationId) {
    return http.post().uri("/v1/forecast/capacity")
      .header("X-Correlation-Id", correlationId == null ? "" : correlationId)
      .contentType(MediaType.APPLICATION_JSON).body(series)
      .retrieve().body(Map.class);
  }
}""")

w(PKG / "integration/IntegrationController.java", """package eg.gov.health.platform.integration;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/integration")
public class IntegrationController {
  private final AnalyticsClient analytics;
  public IntegrationController(AnalyticsClient analytics) { this.analytics = analytics; }
  @PostMapping("/triage-support")
  Map<String, Object> triage(@RequestBody Map<String, Object> vitals, HttpServletRequest req) {
    var out = analytics.triageSupport(vitals, String.valueOf(req.getAttribute("correlationId")));
    return Map.of("ok", true, "advisory_only", true, "requires_human_review", true, "analytics", out == null ? Map.of() : out);
  }
  @PostMapping("/capacity-forecast")
  Map<String, Object> capacity(@RequestBody Map<String, Object> series, HttpServletRequest req) {
    var out = analytics.capacityForecast(series, String.valueOf(req.getAttribute("correlationId")));
    return Map.of("ok", true, "advisory_only", true, "analytics", out == null ? Map.of() : out);
  }
}""")
print("part 4 done")

# ---------------------------------------------------------------- migrations
import pathlib as _pl
seed = _pl.Path.home().joinpath("workspace/egypt-health-dev/catalog/cycle_seed.sql").read_text(encoding="utf-8")
rls = """ALTER TABLE {t} ENABLE ROW LEVEL SECURITY;
ALTER TABLE {t} FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS {t}_facility_isolation ON {t};
CREATE POLICY {t}_facility_isolation ON {t} FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));"""
def rls_for(t): return rls.format(t=t)

v1 = """-- V1 — Core schema (Catalog v6 rebuild, Java/Flyway).
-- Identity, facilities, MPI patients, encounters, audit. RLS on every
-- multi-tenant table; the application connects as app_user (NOBYPASSRLS).
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS facilities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name_ar TEXT NOT NULL, name_en TEXT,
  type TEXT NOT NULL, level TEXT NOT NULL,
  governorate TEXT NOT NULL, directorate TEXT, city TEXT,
  ownership TEXT, bed_capacity INT,
  status TEXT NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS app_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL,
  facility_id UUID REFERENCES facilities(id),
  governorate TEXT, directorate TEXT, full_name TEXT,
  mfa_enabled BOOLEAN NOT NULL DEFAULT false,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS patients (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id),
  governorate TEXT,
  national_id TEXT UNIQUE,
  full_name TEXT NOT NULL,
  birth_date DATE, gender TEXT, phone TEXT, address TEXT,
  identity_status TEXT NOT NULL DEFAULT 'verified',
  merged_into UUID REFERENCES patients(id),
  status TEXT NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_patients_facility_status ON patients (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_patients_name_dob ON patients (full_name, birth_date);

CREATE TABLE IF NOT EXISTS encounters (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id),
  governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  type TEXT NOT NULL DEFAULT 'outpatient',
  chief_complaint TEXT, triage_level INT, news2_score INT,
  status TEXT NOT NULL DEFAULT 'open',
  arrived_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_encounters_facility_status ON encounters (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_encounters_patient ON encounters (patient_id);

CREATE TABLE IF NOT EXISTS audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID, governorate TEXT,
  actor_id TEXT, actor_role TEXT,
  action TEXT, entity_type TEXT, entity_id TEXT,
  payload JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_facility ON audit_logs (facility_id, created_at);
""" + "\n" + rls_for("patients") + "\n" + rls_for("encounters") + "\n" + rls_for("audit_logs") + """
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'app_user') THEN
    CREATE ROLE app_user LOGIN PASSWORD 'change-me' NOBYPASSRLS;
  END IF;
END $$;
GRANT SELECT, INSERT, UPDATE, DELETE ON facilities, app_users, patients, encounters, audit_logs TO app_user;
"""
w(RES / "db/migration/V1__core.sql", v1)

v2 = """-- V2 — Unified Operating Cycle Engine (Catalog v6 cycles 217-254).
-- Definitions seeded verbatim from the catalog; instances are
-- facility-isolated (RLS); events are the append-only transition audit.
CREATE TABLE IF NOT EXISTS cycle_definitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID, governorate TEXT, directorate TEXT,
  code TEXT NOT NULL UNIQUE,
  catalog_number INT NOT NULL UNIQUE,
  title_ar TEXT NOT NULL,
  domain TEXT NOT NULL,
  purpose_ar TEXT,
  specialised_route TEXT,
  sla_hours INT NOT NULL DEFAULT 72,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','archived')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS cycle_instances (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id),
  governorate TEXT, directorate TEXT,
  definition_code TEXT NOT NULL REFERENCES cycle_definitions(code),
  subject_type TEXT, subject_ref TEXT, patient_id UUID REFERENCES patients(id),
  title TEXT NOT NULL, current_step TEXT, protocol_version TEXT,
  correlation_id TEXT NOT NULL,
  idempotency_key TEXT,
  sla_due_at TIMESTAMPTZ,
  escalated BOOLEAN NOT NULL DEFAULT false,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','open','in_progress','on_hold','pending_review','completed','closed','cancelled','failed','corrected','reconciled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cycle_instances_facility_status ON cycle_instances (facility_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cycle_instances_idempotency ON cycle_instances (facility_id, idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE TABLE IF NOT EXISTS cycle_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL, governorate TEXT, directorate TEXT,
  instance_id UUID NOT NULL REFERENCES cycle_instances(id),
  event_type TEXT NOT NULL, from_status TEXT, to_status TEXT,
  actor_id TEXT, actor_role TEXT, reason TEXT,
  status TEXT NOT NULL DEFAULT 'recorded',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_cycle_events_instance ON cycle_events (instance_id, created_at);
""" + "\n" + rls_for("cycle_instances") + "\n" + rls_for("cycle_events") + """
GRANT SELECT, INSERT, UPDATE, DELETE ON cycle_definitions, cycle_instances, cycle_events TO app_user;

INSERT INTO cycle_definitions (code, catalog_number, title_ar, domain, purpose_ar, specialised_route) VALUES
""" + seed + """
ON CONFLICT (code) DO NOTHING;
"""
w(RES / "db/migration/V2__cycles.sql", v2)

# ---------------------------------------------------------------- tests
w(TEST / "clinical/News2CalculatorTest.java", """package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class News2CalculatorTest {
  @Test void normalVitalsScoreZero() {
    var s = News2Calculator.score(new News2Calculator.Vitals(16, 98, false, 120, 80, 37.0, "alert"));
    assertEquals(0, s.total()); assertEquals("low", s.riskBand());
  }
  @Test void deterioratingPatientScoresHigh() {
    var s = News2Calculator.score(new News2Calculator.Vitals(28, 90, true, 95, 125, 39.5, "voice"));
    assertTrue(s.total() >= 7, "expected >=7 got " + s.total()); assertEquals("high", s.riskBand());
  }
  @Test void singleParameterThreeIsLowMedium() {
    var s = News2Calculator.score(new News2Calculator.Vitals(16, 98, false, 120, 80, 34.5, "alert"));
    assertEquals(3, s.total()); assertEquals("low-medium", s.riskBand());
  }
  @Test void oxygenAddsTwo() {
    var a = News2Calculator.score(new News2Calculator.Vitals(16, 97, false, 120, 80, 37.0, "alert"));
    var b = News2Calculator.score(new News2Calculator.Vitals(16, 97, true, 120, 80, 37.0, "alert"));
    assertEquals(a.total() + 2, b.total());
  }
}""")

w(TEST / "cycles/CycleStateMachineTest.java", """package eg.gov.health.platform.cycles;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CycleStateMachineTest {
  @Test void happyPathIsLegal() {
    assertTrue(CycleStateMachine.isLegal("draft", "open"));
    assertTrue(CycleStateMachine.isLegal("open", "in_progress"));
    assertTrue(CycleStateMachine.isLegal("in_progress", "pending_review"));
    assertTrue(CycleStateMachine.isLegal("pending_review", "completed"));
    assertTrue(CycleStateMachine.isLegal("completed", "closed"));
  }
  @Test void skippingReviewGateIsIllegal() {
    assertFalse(CycleStateMachine.isLegal("in_progress", "completed"));
    assertFalse(CycleStateMachine.isLegal("draft", "completed"));
    assertFalse(CycleStateMachine.isLegal("open", "closed"));
  }
  @Test void terminalStatesNeverLeave() {
    for (String to : CycleStateMachine.STATUSES) {
      assertFalse(CycleStateMachine.isLegal("closed", to));
    }
  }
  @Test void exceptionTransitionsRequireReason() {
    assertTrue(CycleStateMachine.REASON_REQUIRED.contains("cancelled"));
    assertTrue(CycleStateMachine.REASON_REQUIRED.contains("corrected"));
    assertTrue(CycleStateMachine.REASON_REQUIRED.contains("reconciled"));
    assertFalse(CycleStateMachine.REASON_REQUIRED.contains("open"));
  }
  @Test void failureReconcilesBeforeClose() {
    assertTrue(CycleStateMachine.isLegal("failed", "reconciled"));
    assertFalse(CycleStateMachine.isLegal("failed", "closed"));
  }
}""")

w(TEST / "patient/DuplicateDetectorTest.java", """package eg.gov.health.platform.patient;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class DuplicateDetectorTest {
  @Test void nameAloneNeverRaisesCandidate() {
    var c = DuplicateDetector.evaluate("Ahmed Ali", LocalDate.of(1990,1,1), null, "Ahmed Ali", LocalDate.of(1985,5,5), null);
    assertFalse(DuplicateDetector.isReviewCandidate(c));
  }
  @Test void namePlusBirthDateRaisesExplainableCandidate() {
    var c = DuplicateDetector.evaluate("Ahmed Ali", LocalDate.of(1990,1,1), null, "Ahmed Ali", LocalDate.of(1990,1,1), null);
    assertTrue(DuplicateDetector.isReviewCandidate(c));
    assertTrue(c.reason().contains("same_name") && c.reason().contains("same_birth_date"));
  }
}""")

w(TEST / "fhir/FhirMappingTest.java", """package eg.gov.health.platform.fhir;
import eg.gov.health.platform.patient.Patient;
import org.junit.jupiter.api.Test;
import java.time.LocalDate; import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class FhirMappingTest {
  @Test void patientMapsToFhirR4() {
    var p = new Patient(null, "القاهرة", "29801011234567", "مريض اختبار", LocalDate.of(1998,1,1), "male", null, null, "verified");
    var f = FhirController.toFhirPatient(p);
    assertEquals("Patient", f.get("resourceType"));
    assertEquals("1998-01-01", f.get("birthDate"));
  }
}""")
print("part 5 done — generator complete")
