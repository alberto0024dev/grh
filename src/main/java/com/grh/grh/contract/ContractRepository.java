package com.grh.grh.contract;


import com.grh.grh.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByEmployee(Employee employee);

    //JPA REPOSITORY YA TE DA LOS METODSO BASICOS SIN ESCRIBIR NADA. HEREDAS AUTOMATICAMENTE:
//save(usuario)        // crear y actualizar
//findById(id)         // buscar por ID
//findAll()            // obtener todos
//deleteById(id)       // eliminar por ID
//existsById(id)       // comprobar si existe
}
