package eg.gov.health.platform.childhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ImmunizationDoseRepository extends JpaRepository<ImmunizationDose, UUID> {
  java.util.List<ImmunizationDose> findByChildId(UUID childId);
}
