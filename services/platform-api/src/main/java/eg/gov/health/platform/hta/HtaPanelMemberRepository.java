package eg.gov.health.platform.hta;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface HtaPanelMemberRepository extends JpaRepository<HtaPanelMember, UUID> {
  long countByAssessmentId(UUID assessmentId); long countByAssessmentIdAndCoiDeclaredFalse(UUID assessmentId);
}
