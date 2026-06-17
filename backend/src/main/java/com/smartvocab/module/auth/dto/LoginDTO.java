package com.smartvocab.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 6, max = 32)
    private String password;
}
