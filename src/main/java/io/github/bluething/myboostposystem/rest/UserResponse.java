package io.github.bluething.myboostposystem.rest;

import java.time.LocalDateTime;

public record UserResponse(Integer id,
                           String firstName,
                           String lastName,
                           String email,
                           String phone,
                           String createdBy,
                           String updatedBy,
                           LocalDateTime createdDatetime,
                           LocalDateTime updatedDatetime) {
}
