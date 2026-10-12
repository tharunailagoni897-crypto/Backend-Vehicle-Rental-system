package com.example.carrental.service;

import com.example.carrental.dto.UserDto;
import com.example.carrental.entity.User;
import com.example.carrental.enums.Role;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<UserDto> getCustomers() {
        return userRepository.findByRole(Role.CUSTOMER).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDto(user);
    }

    public UserDto getUserById(Long id) {
        return mapToDto(findUserEntity(id));
    }

    public UserDto updateUserStatus(Long id, boolean active) {
        User user = findUserEntity(id);
        user.setActive(active);
        return mapToDto(userRepository.save(user));
    }

    private User findUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive()
        );
    }
}
