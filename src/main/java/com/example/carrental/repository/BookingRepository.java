package com.example.carrental.repository;

import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Car;
import com.example.carrental.entity.User;
import com.example.carrental.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByCar(Car car);
    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.car = :car AND b.status IN :statuses AND b.pickupDate < :returnDate AND b.returnDate > :pickupDate")
    List<Booking> findOverlappingBookings(@Param("car") Car car,
                                         @Param("pickupDate") LocalDate pickupDate,
                                         @Param("returnDate") LocalDate returnDate,
                                         @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.car LEFT JOIN FETCH b.user WHERE b.user = :user ORDER BY b.pickupDate DESC")
    List<Booking> findByUserWithDetails(@Param("user") User user);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.car LEFT JOIN FETCH b.user ORDER BY b.pickupDate DESC")
    List<Booking> findAllWithDetails();

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.car LEFT JOIN FETCH b.user WHERE b.car.provider.id = :providerId ORDER BY b.pickupDate DESC")
    List<Booking> findByProviderWithDetails(@Param("providerId") Long providerId);
}
