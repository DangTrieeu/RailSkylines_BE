package com.fourt.railskylines.domain.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not in correct format")
    private String email;
}
