package com.calculator.controller;

import com.calculator.dto.PermissionUpdateRequest;
import com.calculator.model.OperationLog;
import com.calculator.model.User;
import com.calculator.repository.OperationLogRepository;
import com.calculator.service.AdminService;
import com.calculator.service.UserActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserActivityService userActivityService;
    private final OperationLogRepository operationLogRepository;

    public AdminController(AdminService adminService,
                           UserActivityService userActivityService,
                           OperationLogRepository operationLogRepository) {
        this.adminService = adminService;
        this.userActivityService = userActivityService;
        this.operationLogRepository = operationLogRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        Map<String, LocalDateTime> lastActivityMap = new HashMap<>();
        operationLogRepository.findLastActivityPerUser().forEach(row -> {
            lastActivityMap.put((String) row[0], (LocalDateTime) row[1]);
        });

        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("role", user.getRole().name());
            map.put("permissions", user.getPermissions().stream().map(Enum::name).collect(Collectors.toList()));
            map.put("enabled", user.isEnabled());
            map.put("lastActiveAt", lastActivityMap.getOrDefault(user.getUsername(), null));
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String username) {
        User user = adminService.getUserByUsername(username);
        var lastLog = operationLogRepository.findTopByUsernameOrderByTimestampDesc(username);

        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole().name());
        map.put("permissions", user.getPermissions().stream().map(Enum::name).collect(Collectors.toList()));
        map.put("enabled", user.isEnabled());
        map.put("lastActiveAt", lastLog.map(OperationLog::getTimestamp).orElse(null));
        return ResponseEntity.ok(map);
    }

    @PutMapping("/users/{username}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable String username,
            @RequestBody Map<String, String> body) {
        User user = adminService.updateUserRole(
                username,
                com.calculator.model.Role.valueOf(body.get("role"))
        );
        return ResponseEntity.ok(userToMap(user));
    }

    @PutMapping("/users/permissions")
    public ResponseEntity<Map<String, Object>> updateUserPermissions(
            @Valid @RequestBody PermissionUpdateRequest request) {
        User user = adminService.updateUserPermissions(
                request.getTargetUsername(),
                request.getPermissions()
        );
        return ResponseEntity.ok(userToMap(user));
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

    @GetMapping("/activity/status")
    public ResponseEntity<Map<String, LocalDateTime>> getUserActivityStatus() {
        Map<String, LocalDateTime> statusMap = new HashMap<>();
        operationLogRepository.findLastActivityPerUser().forEach(row -> {
            statusMap.put((String) row[0], (LocalDateTime) row[1]);
        });
        return ResponseEntity.ok(statusMap);
    }

    private Map<String, Object> userToMap(User user) {
        var lastLog = operationLogRepository.findTopByUsernameOrderByTimestampDesc(user.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole().name());
        map.put("permissions", user.getPermissions().stream().map(Enum::name).collect(Collectors.toList()));
        map.put("enabled", user.isEnabled());
        map.put("lastActiveAt", lastLog.map(OperationLog::getTimestamp).orElse(null));
        return map;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}