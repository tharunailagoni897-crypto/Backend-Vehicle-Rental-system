package com.example.carrental.service;

import com.example.carrental.dto.CarDto;
import com.example.carrental.entity.Car;
import com.example.carrental.exception.BadRequestException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public CarService(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    public List<CarDto> getAllCars() {
        return carRepository.findByActiveTrue().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<CarDto> getAllCarsForAdmin() {
        return carRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<CarDto> getProviderCars(Long providerId) {
        return carRepository.findByProviderId(providerId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public CarDto createProviderCar(Long providerId, CarDto carDto) {
        ensureRegistrationAvailable(carDto.getRegistrationNumber(), null);
        Car car = mapToEntity(carDto);
        car.setProvider(userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider account not found")));
        return mapToDto(carRepository.save(car));
    }

    @Transactional
    public CarDto updateProviderCar(Long providerId, Long carId, CarDto carDto) {
        Car car = findProviderCar(providerId, carId);
        ensureRegistrationAvailable(carDto.getRegistrationNumber(), carId);
        updateCarFields(car, carDto);
        return mapToDto(carRepository.save(car));
    }

    @Transactional
    public void deleteProviderCar(Long providerId, Long carId) {
        Car car = findProviderCar(providerId, carId);
        car.setActive(false);
        carRepository.save(car);
    }

    public List<CarDto> searchCars(String brand, String model) {
        if (brand != null && !brand.isBlank()) {
            return carRepository.findByBrandContainingIgnoreCaseAndActiveTrue(brand).stream().map(this::mapToDto).collect(Collectors.toList());
        }
        if (model != null && !model.isBlank()) {
            return carRepository.findByModelContainingIgnoreCaseAndActiveTrue(model).stream().map(this::mapToDto).collect(Collectors.toList());
        }
        return getAllCars();
    }

    public CarDto getCarById(Long id) {
        return mapToDto(findCarEntity(id));
    }

    public CarDto createCar(CarDto carDto) {
        ensureRegistrationAvailable(carDto.getRegistrationNumber(), null);
        Car car = mapToEntity(carDto);
        Car saved = carRepository.save(car);
        return mapToDto(saved);
    }

    public CarDto updateCar(Long id, CarDto carDto) {
        Car existing = findCarEntity(id);
        ensureRegistrationAvailable(carDto.getRegistrationNumber(), id);
        updateCarFields(existing, carDto);

        return mapToDto(carRepository.save(existing));
    }

    private void updateCarFields(Car existing, CarDto carDto) {
        existing.setBrand(carDto.getBrand().trim());
        existing.setModel(carDto.getModel().trim());
        existing.setRegistrationNumber(carDto.getRegistrationNumber().trim());
        existing.setDescription(carDto.getDescription());
        existing.setSeatingCapacity(carDto.getSeatingCapacity());
        existing.setDailyPrice(carDto.getDailyPrice());
        existing.setFuelEfficiency(carDto.getFuelEfficiency());
        existing.setImageUrl(carDto.getImageUrl());
        existing.setActive(carDto.isActive());
    }

    @Transactional
    public void deleteCar(Long id) {
        Car car = findCarEntity(id);
        car.setActive(false);
        carRepository.save(car);
    }

    private Car findCarEntity(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
    }

    private Car findProviderCar(Long providerId, Long carId) {
        return carRepository.findByIdAndProviderId(carId, providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found in your fleet"));
    }

    private void ensureRegistrationAvailable(String registrationNumber, Long currentCarId) {
        String registration = registrationNumber.trim();
        if (carRepository.existsByRegistrationNumber(registration)) {
            boolean sameCar = currentCarId != null && carRepository.findById(currentCarId)
                    .map(car -> car.getRegistrationNumber().equalsIgnoreCase(registration))
                    .orElse(false);
            if (!sameCar) throw new BadRequestException("A car with this registration number already exists");
        }
    }

    private CarDto mapToDto(Car car) {
        CarDto dto = new CarDto();
        dto.setId(car.getId());
        if (car.getProvider() != null) {
            dto.setProviderId(car.getProvider().getId());
            dto.setProviderName(car.getProvider().getFirstName() + " " + car.getProvider().getLastName());
        }
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setRegistrationNumber(car.getRegistrationNumber());
        dto.setDescription(car.getDescription());
        dto.setSeatingCapacity(car.getSeatingCapacity());
        dto.setDailyPrice(car.getDailyPrice());
        dto.setFuelEfficiency(car.getFuelEfficiency());
        dto.setImageUrl(car.getImageUrl());
        dto.setActive(car.isActive());
        return dto;
    }

    private Car mapToEntity(CarDto dto) {
        Car car = new Car();
        car.setBrand(dto.getBrand().trim());
        car.setModel(dto.getModel().trim());
        car.setRegistrationNumber(dto.getRegistrationNumber().trim());
        car.setDescription(dto.getDescription());
        car.setSeatingCapacity(dto.getSeatingCapacity());
        car.setDailyPrice(dto.getDailyPrice());
        car.setFuelEfficiency(dto.getFuelEfficiency());
        car.setImageUrl(dto.getImageUrl());
        car.setActive(dto.isActive());
        return car;
    }
}
