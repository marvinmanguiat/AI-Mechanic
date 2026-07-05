package com.marvin.AI_Mechanic.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "car_makes")
public class CarMake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "carMake", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarModel> models;

    public CarMake() {
    }

    public CarMake(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CarModel> getModels() {
        return models;
    }

    public void setModels(List<CarModel> models) {
        this.models = models;
    }
}
