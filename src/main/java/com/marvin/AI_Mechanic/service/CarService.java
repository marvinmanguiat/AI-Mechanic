package com.marvin.AI_Mechanic.service;

import com.marvin.AI_Mechanic.model.CarMake;
import com.marvin.AI_Mechanic.model.CarModel;
import com.marvin.AI_Mechanic.repository.CarMakeRepository;
import com.marvin.AI_Mechanic.repository.CarModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CarService {
    
    @Autowired
    private CarMakeRepository carMakeRepository;
    
    @Autowired
    private CarModelRepository carModelRepository;

    /**
     * Get all car makes
     */
    public List<CarMake> getAllMakes() {
        return carMakeRepository.findAll();
    }

    /**
     * Get a specific car make by ID
     */
    public Optional<CarMake> getMakeById(Long id) {
        return carMakeRepository.findById(id);
    }

    /**
     * Get a car make by name
     */
    public Optional<CarMake> getMakeByName(String name) {
        return carMakeRepository.findByName(name);
    }

    /**
     * Get all models for a specific make
     */
    public List<CarModel> getModelsByMakeId(Long makeId) {
        return carModelRepository.findByCarMakeId(makeId);
    }

    /**
     * Add a new car make
     */
    public CarMake addMake(String makeName) {
        CarMake carMake = new CarMake(makeName);
        return carMakeRepository.save(carMake);
    }

    /**
     * Add a model to a specific make
     */
    public CarModel addModelToMake(Long makeId, String modelName) {
        Optional<CarMake> carMake = carMakeRepository.findById(makeId);
        if (carMake.isPresent()) {
            CarModel model = new CarModel(modelName, carMake.get());
            return carModelRepository.save(model);
        }
        return null;
    }

    /**
     * Initialize database with sample data
     */
    public void initializeSampleData() {
        // Check if data already exists
        if (carMakeRepository.count() > 0) {
            return;
        }

        // Create Honda make
        CarMake honda = carMakeRepository.save(new CarMake("Honda"));
        carModelRepository.save(new CarModel("Civic", honda));
        carModelRepository.save(new CarModel("City", honda));

        // Create Mitsubishi make
        CarMake mitsubishi = carMakeRepository.save(new CarMake("Mitsubishi"));
        carModelRepository.save(new CarModel("Pajero", mitsubishi));
        carModelRepository.save(new CarModel("Montero", mitsubishi));
    }
}
