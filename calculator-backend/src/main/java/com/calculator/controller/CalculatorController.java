package com.calculator.controller;

import com.calculator.dto.CalculationRequest;
import com.calculator.dto.CalculationResponse;
import com.calculator.model.OperationType;
import com.calculator.service.CalculatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calculator")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasAuthority(#request.operation.name())")
    public ResponseEntity<CalculationResponse> calculate(
            @Valid @RequestBody CalculationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(calculatorService.calculate(request, userDetails.getUsername()));
    }

    @GetMapping("/operations")
    public ResponseEntity<Map<String, Object>> getAvailableOperations(
            @AuthenticationPrincipal UserDetails userDetails) {
        var operations = Arrays.stream(OperationType.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        op -> userDetails.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(op.name()))
                ));
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "availableOperations", operations
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(403).body(Map.of("error", "您没有权限执行此操作"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}