package io.github.bluething.myboostposystem.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record ItemCreateRequest(@NotBlank(message = "Item name is required")
                         @Size(max = 500, message = "Item name must not exceed 500 characters")
                         String name,

                         @Size(max = 500, message = "Description must not exceed 500 characters")
                         String description,

                         @NotNull(message = "Price is required")
                         @Min(value = 0, message = "Price must be non-negative")
                                Integer price,

                         @NotNull(message = "Cost is required")
                         @Min(value = 0, message = "Cost must be non-negative")
                         Integer cost,

                         @NotBlank(message = "Created by is required")
                         String createdBy) {
}
