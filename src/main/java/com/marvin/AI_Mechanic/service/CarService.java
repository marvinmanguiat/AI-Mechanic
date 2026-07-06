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

        // Create Mercedez Benz make
        CarMake mercedes = carMakeRepository.save(new CarMake("Mercedes Benz"));
        carModelRepository.save(new CarModel("C-Class", mercedes));
        carModelRepository.save(new CarModel("E-Class", mercedes));
        carModelRepository.save(new CarModel("S-Class", mercedes));
        carModelRepository.save(new CarModel("GLA", mercedes));
        carModelRepository.save(new CarModel("GLC", mercedes));
        carModelRepository.save(new CarModel("GLE", mercedes));
        carModelRepository.save(new CarModel("GLS", mercedes));

        CarMake bmw = carMakeRepository.save(new CarMake("BMW"));
        carModelRepository.save(new CarModel("3 Series", bmw));
        carModelRepository.save(new CarModel("5 Series", bmw));
        carModelRepository.save(new CarModel("7 Series", bmw));
        carModelRepository.save(new CarModel("X1", bmw));
        carModelRepository.save(new CarModel("X3", bmw));
        carModelRepository.save(new CarModel("X5", bmw));
        carModelRepository.save(new CarModel("X7", bmw));
        carModelRepository.save(new CarModel("Z4", bmw));
        carModelRepository.save(new CarModel("i3", bmw));
        carModelRepository.save(new CarModel("i8", bmw));
        carModelRepository.save(new CarModel("M3", bmw));
        carModelRepository.save(new CarModel("M4", bmw));
        carModelRepository.save(new CarModel("M5", bmw));
        carModelRepository.save(new CarModel("M6", bmw));



        // Create Honda make
        CarMake honda = carMakeRepository.save(new CarMake("Honda"));
        carModelRepository.save(new CarModel("Civic", honda));
        carModelRepository.save(new CarModel("City", honda));
        carModelRepository.save(new CarModel("Accord", honda));
        carModelRepository.save(new CarModel("CR-V", honda));
        carModelRepository.save(new CarModel("HR-V", honda));
        carModelRepository.save(new CarModel("Pilot", honda));
        carModelRepository.save(new CarModel("Odyssey", honda));
        carModelRepository.save(new CarModel("Fit", honda));
        carModelRepository.save(new CarModel("Insight", honda));
        carModelRepository.save(new CarModel("Passport", honda));
        carModelRepository.save(new CarModel("Ridgeline", honda));
        carModelRepository.save(new CarModel("S2000", honda));
        carModelRepository.save(new CarModel("NSX", honda));
        carModelRepository.save(new CarModel("Prelude", honda));
        carModelRepository.save(new CarModel("Element", honda));
        carModelRepository.save(new CarModel("CR-Z", honda));
        carModelRepository.save(new CarModel("FR-V", honda));
        carModelRepository.save(new CarModel("Jazz", honda));



        // Create Mitsubishi make
        CarMake mitsubishi = carMakeRepository.save(new CarMake("Mitsubishi"));
        carModelRepository.save(new CarModel("Pajero", mitsubishi));
        carModelRepository.save(new CarModel("Montero", mitsubishi));
        carModelRepository.save(new CarModel("Outlander", mitsubishi));
        carModelRepository.save(new CarModel("Lancer", mitsubishi));
        carModelRepository.save(new CarModel("Eclipse", mitsubishi));
        carModelRepository.save(new CarModel("ASX", mitsubishi));
        carModelRepository.save(new CarModel("Mirage", mitsubishi));
        carModelRepository.save(new CarModel("Triton", mitsubishi));
        carModelRepository.save(new CarModel("Colt", mitsubishi));
        carModelRepository.save(new CarModel("Galant", mitsubishi));
        carModelRepository.save(new CarModel("3000GT", mitsubishi));
        carModelRepository.save(new CarModel("Space Star", mitsubishi));
        carModelRepository.save(new CarModel("i-MiEV", mitsubishi));
        carModelRepository.save(new CarModel("L200", mitsubishi));
        carModelRepository.save(new CarModel("Lancer Evolution", mitsubishi));
        carModelRepository.save(new CarModel("Grandis", mitsubishi));
        carModelRepository.save(new CarModel("Carisma", mitsubishi));

         // Create Toyota make
        CarMake toyota = carMakeRepository.save(new CarMake("Toyota"));
        carModelRepository.save(new CarModel("Corolla", toyota));
        carModelRepository.save(new CarModel("Camry", toyota));
        carModelRepository.save(new CarModel("RAV4", toyota));
        carModelRepository.save(new CarModel("Hilux", toyota));
        carModelRepository.save(new CarModel("Yaris", toyota));
        carModelRepository.save(new CarModel("Prius", toyota));
        carModelRepository.save(new CarModel("Land Cruiser", toyota));
        carModelRepository.save(new CarModel("Fortuner", toyota));
        carModelRepository.save(new CarModel("C-HR", toyota));
        carModelRepository.save(new CarModel("Avalon", toyota));
        carModelRepository.save(new CarModel("Supra", toyota));

        // Create Ford make
         CarMake ford = carMakeRepository.save(new CarMake("Ford"));
        carModelRepository.save(new CarModel("F-150", ford));
        carModelRepository.save(new CarModel("Mustang", ford));
        carModelRepository.save(new CarModel("Explorer", ford));
        carModelRepository.save(new CarModel("Escape", ford));
        carModelRepository.save(new CarModel("Edge", ford));
        carModelRepository.save(new CarModel("Fusion", ford));
        carModelRepository.save(new CarModel("Taurus", ford));
        carModelRepository.save(new CarModel("Focus", ford));
        carModelRepository.save(new CarModel("Fiesta", ford));
        carModelRepository.save(new CarModel("Bronco", ford));
        carModelRepository.save(new CarModel("Ranger", ford));
        carModelRepository.save(new CarModel("Transit", ford));
        carModelRepository.save(new CarModel("Expedition", ford));
        carModelRepository.save(new CarModel("Maverick", ford));

        // Create Chevrolet make
         CarMake chevrolet = carMakeRepository.save(new CarMake("Chevrolet"));
        carModelRepository.save(new CarModel("Silverado", chevrolet));
        carModelRepository.save(new CarModel("Equinox", chevrolet));
        carModelRepository.save(new CarModel("Malibu", chevrolet));
        carModelRepository.save(new CarModel("Traverse", chevrolet));
        carModelRepository.save(new CarModel("Tahoe", chevrolet));
        carModelRepository.save(new CarModel("Camaro", chevrolet));
        carModelRepository.save(new CarModel("Colorado", chevrolet));
        carModelRepository.save(new CarModel("Impala", chevrolet));
        carModelRepository.save(new CarModel("Blazer", chevrolet));
        carModelRepository.save(new CarModel("Tahoe", chevrolet));
        carModelRepository.save(new CarModel("Suburban", chevrolet));
        carModelRepository.save(new CarModel("Traverse", chevrolet));

        // Create Subaru make
         CarMake subaru = carMakeRepository.save(new CarMake("Subaru"));
        carModelRepository.save(new CarModel("Impreza", subaru));
        carModelRepository.save(new CarModel("Outback", subaru));
        carModelRepository.save(new CarModel("Forester", subaru));
        carModelRepository.save(new CarModel("Crosstrek", subaru));
        carModelRepository.save(new CarModel("Ascent", subaru));
        carModelRepository.save(new CarModel("WRX", subaru));   



    }
}
