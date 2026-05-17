package com.calculator.dto;

import com.calculator.model.OperationType;
import jakarta.validation.constraints.NotNull;

public class CalculationRequest {

    @NotNull(message = "运算类型不能为空")
    private OperationType operation;

    private Double operandA;

    private Double operandB;

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public Double getOperandA() {
        return operandA;
    }

    public void setOperandA(Double operandA) {
        this.operandA = operandA;
    }

    public Double getOperandB() {
        return operandB;
    }

    public void setOperandB(Double operandB) {
        this.operandB = operandB;
    }
}