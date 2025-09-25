package io.github.bluething.myboostposystem.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

record PoCreateRequest(@NotBlank String poNumber,
                              @NotNull Integer supplierId,
                              @NotNull LocalDate orderDate,
                              @Size(max = 50) String status,
                              @Size(max = 255) String remarks,
                              @NotNull @Size(min = 1) List<@Valid PoDetailRequest> details) {
}
