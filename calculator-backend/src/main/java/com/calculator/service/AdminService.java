package com.calculator.service;

import com.calculator.model.OperationType;
import com.calculator.model.Role;
import com.calculator.model.User;
import com.calculator.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }

    public User updateUserRole(String username, Role newRole) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User updateUserPermissions(String username, Set<OperationType> permissions) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        user.setPermissions(permissions);
        return userRepository.save(user);
    }

    public void toggleUserEnabled(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        userRepository.delete(user);
    }
}