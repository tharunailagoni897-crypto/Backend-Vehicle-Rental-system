package com.example.carrental.repository;

import com.example.carrental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByActiveTrue();
    List<Car> findByBrandContainingIgnoreCaseAndActiveTrue(String brand);
    List<Car> findByModelContainingIgnoreCaseAndActiveTrue(String model);
    List<Car> findByActiveTrueOrderByDailyPriceAsc();
    List<Car> findByProviderId(Long providerId);
    Optional<Car> findByIdAndProviderId(Long id, Long providerId);
    boolean existsByRegistrationNumber(String registrationNumber);
}
