package com.example.carrental.config;

import com.example.carrental.entity.Car;
import com.example.carrental.entity.User;
import com.example.carrental.enums.Role;
import com.example.carrental.repository.CarRepository;
import com.example.carrental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@carrental.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, CarRepository carRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhone("+123456789");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }

        if (carRepository.count() == 0) {
            Car car = new Car();
            car.setBrand("Toyota");
            car.setModel("Corolla");
            car.setRegistrationNumber("ABC-1234");
            car.setDescription("Reliable compact sedan ideal for city and highway travel.");
            car.setSeatingCapacity(5);
            car.setDailyPrice(new BigDecimal("45.00"));
            car.setFuelEfficiency(new BigDecimal("18.00"));
            car.setImageUrl("https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=900&q=80");
            car.setActive(true);
            carRepository.save(car);

            Car another = new Car();
            another.setBrand("BMW");
            another.setModel("X5");
            another.setRegistrationNumber("XYZ-9876");
            another.setDescription("Luxury SUV with premium comfort and advanced safety features.");
            another.setSeatingCapacity(7);
            another.setDailyPrice(new BigDecimal("120.00"));
            another.setFuelEfficiency(new BigDecimal("12.00"));
            another.setImageUrl("https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=900&q=80");
            another.setActive(true);
            carRepository.save(another);
        }
    }
}
