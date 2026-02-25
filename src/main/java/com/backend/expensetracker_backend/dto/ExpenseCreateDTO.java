package com.backend.expensetracker_backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCreateDTO {

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;
}