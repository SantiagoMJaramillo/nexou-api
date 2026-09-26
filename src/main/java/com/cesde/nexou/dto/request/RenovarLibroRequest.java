package com.cesde.nexou.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenovarLibroRequest {
    @NotNull 
    @Positive 
    private Integer diasExtra;
}
