package eg.gov.health.platform.config;
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
}
