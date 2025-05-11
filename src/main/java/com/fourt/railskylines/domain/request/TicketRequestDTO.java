package com.fourt.railskylines.domain.request;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import com.fourt.railskylines.util.constant.CustomerObjectEnum;
@Getter
@Setter
public class TicketRequestDTO {
    @NotEmpty
    private String name;

    @NotEmpty
    private String citizenId;
    
    private CustomerObjectEnum customerObject;

    private Long boardingStationId;
    private Long alightingStationId;
}
