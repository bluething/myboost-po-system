package io.github.bluething.myboostposystem.rest.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

record CreatePORequest(@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                       @NotNull(message = "Datetime is required")
                       LocalDateTime datetime,

                       @Size(max = 500, message = "Description must not exceed 500 characters")
                       String description,
                       @NotNull @Positive Long totalPrice,
                       @NotNull @Positive Long totalCost,
                       @Valid
                       @NotEmpty(message = "Purchase order details cannot be empty")
                       List<@Valid PoDetailRequest> details) {
}
