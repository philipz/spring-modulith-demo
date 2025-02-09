package dev.cat.modular.monolith.dto.calculator;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CalculatorRequest(@NotNull @Min(0) double weight,
                                @NotBlank String addressFrom,
                                @NotBlank String addressTo) {
}
