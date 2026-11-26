package eg.gov.health.platform.schoolhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface StudentHealthRecordRepository extends JpaRepository<StudentHealthRecord, UUID> {
  long countByInstitutionRef(String institutionRef);
}
