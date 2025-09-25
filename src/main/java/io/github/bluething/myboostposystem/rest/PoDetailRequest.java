package io.github.bluething.myboostposystem.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

record PoDetailRequest(@NotNull Integer itemId,
                       @NotNull @Positive Integer quantity,
                       @NotNull @Positive Long unitPrice,
                       @Size(max = 255) String note) {
}
