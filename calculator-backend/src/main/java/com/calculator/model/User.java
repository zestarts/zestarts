package com.calculator.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<OperationType> permissions = new HashSet<>();

    @Column(nullable = false)
    private boolean enabled = true;

    public User() {
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.permissions = getDefaultPermissions(role);
    }

    private Set<OperationType> getDefaultPermissions(Role role) {
        Set<OperationType> perms = new HashSet<>();
        switch (role) {
            case ROLE_USER:
                perms.add(OperationType.ADD);
                perms.add(OperationType.SUBTRACT);
                break;
            case ROLE_VIP:
            case ROLE_ADMIN:
                perms.add(OperationType.ADD);
                perms.add(OperationType.SUBTRACT);
                perms.add(OperationType.MULTIPLY);
                perms.add(OperationType.DIVIDE);
                perms.add(OperationType.SQRT);
                perms.add(OperationType.POWER);
                break;
        }
        return perms;
    }

    public boolean hasPermission(OperationType operation) {
        return permissions.contains(operation);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<OperationType> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<OperationType> permissions) {
        this.permissions = permissions;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}