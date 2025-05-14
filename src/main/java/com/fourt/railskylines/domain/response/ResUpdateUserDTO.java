package com.fourt.railskylines.domain.response;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUpdateUserDTO {
    private long userId;
    private String email;
    private String fullName;
    private String citizenId;
    private String phoneNumber;
    private String avatar;
    private Instant updatedAt;
}
