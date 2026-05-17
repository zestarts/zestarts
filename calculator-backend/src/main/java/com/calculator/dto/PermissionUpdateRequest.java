package com.calculator.dto;

import com.calculator.model.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class PermissionUpdateRequest {

    @NotBlank(message = "目标用户名不能为空")
    private String targetUsername;

    @NotEmpty(message = "权限列表不能为空")
    private Set<OperationType> permissions;

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public Set<OperationType> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<OperationType> permissions) {
        this.permissions = permissions;
    }
}