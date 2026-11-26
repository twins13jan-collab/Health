package eg.gov.health.platform.preventive;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ScreeningInvitationRepository extends JpaRepository<ScreeningInvitation, UUID> {
  long countByProgramId(UUID programId); long countByProgramIdAndAttendance(UUID programId, String attendance);
}
