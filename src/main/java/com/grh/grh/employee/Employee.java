package com.grh.grh.employee;


import com.grh.grh.contract.Contract;
import com.grh.grh.department.Department;
import com.grh.grh.position.Position;
import com.grh.grh.security.CryptoConverter;
import com.grh.grh.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name="employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"contract", "positions", "department", "usuario"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String firstSurname;

    @Column(length = 50)
    private String secondSurname;

    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false, length = 100)
    private String idNumber;

    @Column(unique = true, nullable = false, length = 64)
    private String idNumberHash;


    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 200)
    private String emergencyContact;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, length = 9)
    private boolean active;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(unique = true, nullable = false, length = 200)
    private String email;

    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false, length = 100)
    private String socialSecurityNumber;

    @Column(unique = true, nullable = false, length = 64)
    private String socialSecurityHash;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="contract_id") // por logica convendria en true mejor ?¿?
    private Contract contract;

    @ManyToMany
    @JoinTable(
            name="employee_positions",  //tabla intermeedia. con dos columnas employee:_id yt position
            joinColumns = @JoinColumn(name="employee_id"),  // con  la key de esta clase
            inverseJoinColumns = @JoinColumn(name="position_id")  // con la fl de la que une
    )
    private List<Position> positions;

    @ManyToOne
    @JoinColumn(name="department_id")
    private Department department;

    @OneToOne
    @JoinColumn(name="usuario_id", nullable = false)
    private Usuario usuario;

    public static Employee toEntity(EmployeeRequestDTO dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName().trim());
        employee.setActive(true);
        employee.setBirthDate(dto.getBirthDate());
        employee.setEmail(dto.getEmail().trim().toLowerCase());
        employee.setFirstSurname(dto.getFirstSurname().trim());
        employee.setSecondSurname(clean(dto.getSecondSurname()));
        employee.setIdNumber(dto.getIdNumber().trim().toUpperCase());
        employee.setAddress(dto.getAddress().trim());
        employee.setSocialSecurityNumber(dto.getSocialSecurityNumber());
        employee.setEmergencyContact(dto.getEmergencyContact());
        employee.setPhoneNumber(dto.getPhoneNumber());
        return employee;
    }

    // Recorta espacios y convierte el vacío en null
    static String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }


}
