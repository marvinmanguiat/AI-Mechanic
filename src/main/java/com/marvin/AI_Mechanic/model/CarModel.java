package com.marvin.AI_Mechanic.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "car_models")
public class CarModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_make_id", nullable = false)
    @JsonIgnore
    private CarMake carMake;

    public CarModel() {
    }

    public CarModel(String name, CarMake carMake) {
        this.name = name;
        this.carMake = carMake;
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

    public CarMake getCarMake() {
        return carMake;
    }

    public void setCarMake(CarMake carMake) {
        this.carMake = carMake;
    }
}
