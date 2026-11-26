package eg.gov.health.platform.orglearning;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface LessonLearnedRepository extends JpaRepository<LessonLearned, UUID> {
}
