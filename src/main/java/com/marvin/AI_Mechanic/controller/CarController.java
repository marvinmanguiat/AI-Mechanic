package com.marvin.AI_Mechanic.controller;

import com.marvin.AI_Mechanic.model.CarMake;
import com.marvin.AI_Mechanic.model.CarModel;
import com.marvin.AI_Mechanic.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins = "http://192.168.1.7:3000")
public class CarController {
    
    @Autowired
    private CarService carService;

    /**
     * Get all car makes
     * GET /api/cars/makes
     */
    @GetMapping("/makes")
    public ResponseEntity<List<CarMake>> getAllMakes() {
        List<CarMake> makes = carService.getAllMakes();
        return ResponseEntity.ok(makes);
    }

    /**
     * Get a specific make by ID
     * GET /api/cars/makes/{id}
     */
    @GetMapping("/makes/{id}")
    public ResponseEntity<?> getMakeById(@PathVariable Long id) {
        Optional<CarMake> make = carService.getMakeById(id);
        if (make.isPresent()) {
            return ResponseEntity.ok(make.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get models for a specific make
     * GET /api/cars/makes/{id}/models
     */
    @GetMapping("/makes/{id}/models")
    public ResponseEntity<List<CarModel>> getModelsByMakeId(@PathVariable Long id) {
        // Verify that the make exists
        Optional<CarMake> make = carService.getMakeById(id);
        if (make.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<CarModel> models = carService.getModelsByMakeId(id);
        return ResponseEntity.ok(models);
    }

    /**
     * Get all models for all makes (with nested structure)
     * GET /api/cars/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<CarMake>> getAllMakesWithModels() {
        List<CarMake> makes = carService.getAllMakes();
        return ResponseEntity.ok(makes);
    }

    /**
     * Add a new car make
     * POST /api/cars/makes
     */
    @PostMapping("/makes")
    public ResponseEntity<CarMake> addMake(@RequestParam String name) {
        CarMake make = carService.addMake(name);
        return ResponseEntity.status(201).body(make);
    }

    /**
     * Add a model to a specific make
     * POST /api/cars/makes/{id}/models
     */
    @PostMapping("/makes/{id}/models")
    public ResponseEntity<?> addModelToMake(@PathVariable Long id, @RequestParam String name) {
        CarModel model = carService.addModelToMake(id, name);
        if (model != null) {
            return ResponseEntity.status(201).body(model);
        }
        return ResponseEntity.notFound().build();
    }
}
