package com.grh.grh.position;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position,Long> {

    public List<Position> findByIdIn(List<Long> ids);

    boolean existsByNameIgnoreCaseAndJobCategory(String name, JobCategory jobCategory);

    boolean existsByNameIgnoreCaseAndJobCategoryAndIdNot(String name, JobCategory jobCategory, Long id);
}
