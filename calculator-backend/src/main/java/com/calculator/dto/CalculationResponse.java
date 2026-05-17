package com.calculator.dto;

public class CalculationResponse {

    private String expression;
    private String result;
    private String operation;

    public CalculationResponse() {
    }

    public CalculationResponse(String expression, String result, String operation) {
        this.expression = expression;
        this.result = result;
        this.operation = operation;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}