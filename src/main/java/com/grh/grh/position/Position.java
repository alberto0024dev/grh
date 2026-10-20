package com.grh.grh.position;

import com.grh.grh.employee.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name="positions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"employees"})
public class Position {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobCategory jobCategory;


    @ManyToMany(mappedBy = "positions")
    private List<Employee> employees;

    public static Position fromRequest(PositionRequestDTO request) {
        Position position = new Position();
        position.setName(request.getName().trim());
        position.setJobCategory(request.getJobCategory());
        return position;
    }

}
