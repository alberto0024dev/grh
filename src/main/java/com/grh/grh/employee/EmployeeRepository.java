package com.grh.grh.employee;

import com.grh.grh.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    Optional<Employee> findByUsuario(Usuario usuario);
    boolean existsByIdNumberHash(String hash);
    boolean existsBySocialSecurityHash(String hash);

    boolean existsByEmail(String email);
}
