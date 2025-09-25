package io.github.bluething.myboostposystem.rest.po;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

record PoDetailRequest(@NotNull Integer itemId,
                       @NotNull @Positive Integer quantity,
                       @NotNull @Positive Long unitPrice,
                       @NotNull @Positive Long cost) {
}
