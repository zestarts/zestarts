package com.calculator.controller;

import com.calculator.dto.PermissionUpdateRequest;
import com.calculator.model.OperationLog;
import com.calculator.model.User;
import com.calculator.service.AdminService;
import com.calculator.service.UserActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserActivityService userActivityService;

    public AdminController(AdminService adminService,
                           UserActivityService userActivityService) {
        this.adminService = adminService;
        this.userActivityService = userActivityService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        return ResponseEntity.ok(adminService.getUserByUsername(username));
    }

    @PutMapping("/users/{username}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable String username,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.updateUserRole(
                username,
                com.calculator.model.Role.valueOf(body.get("role"))
        ));
    }

    @PutMapping("/users/permissions")
    public ResponseEntity<User> updateUserPermissions(
            @Valid @RequestBody PermissionUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUserPermissions(
                request.getTargetUsername(),
                request.getPermissions()
        ));
    }

    @PutMapping("/users/{username}/toggle")
    public ResponseEntity<Map<String, String>> toggleUser(@PathVariable String username) {
        adminService.toggleUserEnabled(username);
        return ResponseEntity.ok(Map.of("message", "用户状态已切换"));
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        adminService.deleteUser(username);
        return ResponseEntity.ok(Map.of("message", "用户已删除"));
    }

    @GetMapping("/activity/recent")
    public ResponseEntity<List<OperationLog>> getRecentActivity() {
        return ResponseEntity.ok(userActivityService.getRecentLogs());
    }

    @GetMapping("/activity/user/{username}")
    public ResponseEntity<List<OperationLog>> getUserActivity(@PathVariable String username) {
        return ResponseEntity.ok(userActivityService.getUserLogs(username));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}