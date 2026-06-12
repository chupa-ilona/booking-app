package spring.bookingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.bookingapp.model.Role;
import spring.bookingapp.model.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleName name);
}