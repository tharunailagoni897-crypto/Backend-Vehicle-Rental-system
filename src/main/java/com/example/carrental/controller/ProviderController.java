package com.example.carrental.controller;

import com.example.carrental.dto.BookingDto;
import com.example.carrental.dto.CarDto;
import com.example.carrental.entity.User;
import com.example.carrental.enums.BookingStatus;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.service.BookingService;
import com.example.carrental.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provider")
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderController {

    private final CarService carService;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    public ProviderController(CarService carService, BookingService bookingService, UserRepository userRepository) {
        this.carService = carService;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/cars")
    public ResponseEntity<List<CarDto>> getMyCars(Authentication authentication) {
        return ResponseEntity.ok(carService.getProviderCars(getProvider(authentication).getId()));
    }

    @PostMapping("/cars")
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarDto carDto, Authentication authentication) {
        return ResponseEntity.ok(carService.createProviderCar(getProvider(authentication).getId(), carDto));
    }

    @PutMapping("/cars/{id}")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long id,
                                            @Valid @RequestBody CarDto carDto,
                                            Authentication authentication) {
        return ResponseEntity.ok(carService.updateProviderCar(getProvider(authentication).getId(), id, carDto));
    }

    @DeleteMapping("/cars/{id}")
    public ResponseEntity<Void> removeCar(@PathVariable Long id, Authentication authentication) {
        carService.deleteProviderCar(getProvider(authentication).getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDto>> getBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingsForProvider(getProvider(authentication).getId()));
    }

    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long id,
                                                           @RequestParam BookingStatus status,
                                                           Authentication authentication) {
        return ResponseEntity.ok(bookingService.updateProviderBookingStatus(
                getProvider(authentication).getId(), id, status));
    }

    private User getProvider(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Provider account not found"));
    }
}
