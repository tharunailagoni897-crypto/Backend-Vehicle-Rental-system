package com.example.carrental.dto;

import com.example.carrental.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDto {
    private Long id;
    private Long carId;
    private String carName;
    private Long userId;
    private String userName;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private BigDecimal totalPrice;
    private BigDecimal dailyRateAtBooking;
    private BookingStatus status;

    public BookingDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getDailyRateAtBooking() {
        return dailyRateAtBooking;
    }

    public void setDailyRateAtBooking(BigDecimal dailyRateAtBooking) {
        this.dailyRateAtBooking = dailyRateAtBooking;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
