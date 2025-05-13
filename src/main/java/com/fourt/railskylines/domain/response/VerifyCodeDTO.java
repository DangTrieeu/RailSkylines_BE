package com.fourt.railskylines.domain.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCodeDTO {
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Code cannot be blank")
    private String code;

}