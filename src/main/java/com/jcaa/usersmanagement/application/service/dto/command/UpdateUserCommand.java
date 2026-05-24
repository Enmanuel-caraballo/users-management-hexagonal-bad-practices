package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserCommand(
    @NotBlank String id,
    @NotBlank
    @Size(min = 3)
    String name,
    @NotBlank
    @Email
    String email,
    String password,
    @NotBlank String role,
    @NotBlank String status) {
}
