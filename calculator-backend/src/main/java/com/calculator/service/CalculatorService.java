package com.calculator.service;

import com.calculator.dto.CalculationRequest;
import com.calculator.dto.CalculationResponse;
import com.calculator.model.OperationType;
import com.calculator.model.User;
import com.calculator.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    private final UserRepository userRepository;
    private final UserActivityService userActivityService;

    public CalculatorService(UserRepository userRepository,
                             UserActivityService userActivityService) {
        this.userRepository = userRepository;
        this.userActivityService = userActivityService;
    }

    public CalculationResponse calculate(CalculationRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        OperationType operation = request.getOperation();

        if (!user.hasPermission(operation)) {
            throw new RuntimeException("您没有权限执行 " + operation + " 运算");
        }

        double result;
        String expression;

        switch (operation) {
            case ADD -> {
                validateTwoOperands(request);
                result = request.getOperandA() + request.getOperandB();
                expression = request.getOperandA() + " + " + request.getOperandB();
            }
            case SUBTRACT -> {
                validateTwoOperands(request);
                result = request.getOperandA() - request.getOperandB();
                expression = request.getOperandA() + " - " + request.getOperandB();
            }
            case MULTIPLY -> {
                validateTwoOperands(request);
                result = request.getOperandA() * request.getOperandB();
                expression = request.getOperandA() + " × " + request.getOperandB();
            }
            case DIVIDE -> {
                validateTwoOperands(request);
                if (request.getOperandB() == 0) {
                    throw new RuntimeException("除数不能为零");
                }
                result = request.getOperandA() / request.getOperandB();
                expression = request.getOperandA() + " ÷ " + request.getOperandB();
            }
            case SQRT -> {
                validateOneOperand(request, "operandA");
                if (request.getOperandA() < 0) {
                    throw new RuntimeException("不能对负数开平方根");
                }
                result = Math.sqrt(request.getOperandA());
                expression = "√" + request.getOperandA();
            }
            case POWER -> {
                validateTwoOperands(request);
                result = Math.pow(request.getOperandA(), request.getOperandB());
                expression = request.getOperandA() + " ^ " + request.getOperandB();
            }
            default -> throw new RuntimeException("不支持的运算类型");
        }

        String resultStr = formatResult(result);
        userActivityService.recordAndBroadcast(username, operation, expression, resultStr);

        return new CalculationResponse(expression, resultStr, operation.name());
    }

    private void validateTwoOperands(CalculationRequest request) {
        if (request.getOperandA() == null || request.getOperandB() == null) {
            throw new RuntimeException("该运算需要两个操作数");
        }
    }

    private void validateOneOperand(CalculationRequest request, String operandName) {
        if ("operandA".equals(operandName) && request.getOperandA() == null) {
            throw new RuntimeException("该运算需要一个操作数");
        }
    }

    private String formatResult(double result) {
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return String.valueOf((long) result);
        }
        return String.format("%.4f", result);
    }
}