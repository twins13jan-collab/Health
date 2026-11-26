package eg.gov.health.platform.accessmatrix;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {}
