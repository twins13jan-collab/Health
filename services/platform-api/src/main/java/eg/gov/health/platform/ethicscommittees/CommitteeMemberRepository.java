package eg.gov.health.platform.ethicscommittees;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CommitteeMemberRepository extends JpaRepository<CommitteeMember, UUID> {
  java.util.List<CommitteeMember> findByCommitteeId(UUID committeeId);
}
