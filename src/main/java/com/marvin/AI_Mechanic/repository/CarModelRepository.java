package com.marvin.AI_Mechanic.repository;

import com.marvin.AI_Mechanic.model.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Long> {
    List<CarModel> findByCarMakeId(Long carMakeId);
}
