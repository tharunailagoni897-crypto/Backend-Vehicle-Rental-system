package com.example.carrental.service;

import com.example.carrental.dto.BookingDto;
import com.example.carrental.dto.BookingRequest;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Car;
import com.example.carrental.entity.User;
import com.example.carrental.enums.BookingStatus;
import com.example.carrental.exception.BadRequestException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, CarRepository carRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    public List<BookingDto> getBookingsForCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return bookingRepository.findByUserWithDetails(user).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAllWithDetails().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsForProvider(Long providerId) {
        return bookingRepository.findByProviderWithDetails(providerId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public BookingDto createBooking(Long userId, BookingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + request.getCarId()));

        validateDates(request.getPickupDate(), request.getReturnDate());
        validateCarAvailability(car, request.getPickupDate(), request.getReturnDate());

        long days = ChronoUnit.DAYS.between(request.getPickupDate(), request.getReturnDate());
        if (days <= 0) {
            throw new BadRequestException("Return date must be after pickup date");
        }

        BigDecimal dailyRateAtBooking = car.getDailyPrice();
        BigDecimal totalPrice = dailyRateAtBooking.multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setCar(car);
        booking.setUser(user);
        booking.setPickupDate(request.getPickupDate());
        booking.setReturnDate(request.getReturnDate());
        booking.setDailyRateAtBooking(dailyRateAtBooking);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        return mapToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto cancelBooking(Long bookingId, Long userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!isAdmin && !Objects.equals(booking.getUser().getId(), userId)) {
            throw new BadRequestException("You are not allowed to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("This booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return mapToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto updateBookingStatus(Long bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        booking.setStatus(status);
        return mapToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto updateProviderBookingStatus(Long providerId, Long bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        if (booking.getCar().getProvider() == null ||
                !Objects.equals(booking.getCar().getProvider().getId(), providerId)) {
            throw new BadRequestException("This booking is not for a car in your fleet");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only pending or confirmed bookings can be updated");
        }
        if (status != BookingStatus.CONFIRMED &&
                status != BookingStatus.CANCELLED &&
                status != BookingStatus.COMPLETED) {
            throw new BadRequestException("Providers can confirm, cancel, or complete bookings");
        }
        booking.setStatus(status);
        return mapToDto(bookingRepository.save(booking));
    }

    public BookingDto getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return mapToDto(booking);
    }

    private void validateDates(LocalDate pickupDate, LocalDate returnDate) {
        LocalDate today = LocalDate.now();
        if (pickupDate == null || returnDate == null) {
            throw new BadRequestException("Pickup date and return date are required");
        }
        if (pickupDate.isBefore(today)) {
            throw new BadRequestException("Pickup date cannot be in the past");
        }
        if (!returnDate.isAfter(pickupDate)) {
            throw new BadRequestException("Return date must be after pickup date");
        }
    }

    private void validateCarAvailability(Car car, LocalDate pickupDate, LocalDate returnDate) {
        if (!car.isActive()) {
            throw new BadRequestException("This car is not available for booking");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                car,
                pickupDate,
                returnDate,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Car is already booked for the selected dates");
        }
    }

    private BookingDto mapToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setCarId(booking.getCar().getId());
        dto.setCarName(booking.getCar().getBrand() + " " + booking.getCar().getModel());
        dto.setUserId(booking.getUser().getId());
        dto.setUserName(booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        dto.setPickupDate(booking.getPickupDate());
        dto.setReturnDate(booking.getReturnDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setDailyRateAtBooking(booking.getDailyRateAtBooking());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
