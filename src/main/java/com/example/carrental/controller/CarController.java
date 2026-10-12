package com.example.carrental.controller;

import com.example.carrental.dto.CarDto;
import com.example.carrental.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/cars")
    public ResponseEntity<List<CarDto>> getAllCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model) {
        return ResponseEntity.ok(carService.searchCars(brand, model));
    }

    @GetMapping("/admin/cars")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CarDto>> getAllCarsForAdmin() {
        return ResponseEntity.ok(carService.getAllCarsForAdmin());
    }

    @GetMapping("/cars/{id}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @PostMapping("/admin/cars")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarDto carDto) {
        return ResponseEntity.ok(carService.createCar(carDto));
    }

    @PutMapping("/admin/cars/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long id, @Valid @RequestBody CarDto carDto) {
        return ResponseEntity.ok(carService.updateCar(id, carDto));
    }

    @DeleteMapping("/admin/cars/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }
}
