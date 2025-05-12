package com.fourt.railskylines.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ResResetPassword {
    private String email;
    private String newPassword;
}
