package io.github.bluething.myboostposystem.rest;

import java.time.Instant;

record UserResponse(Integer id,
                           String firstName,
                           String lastName,
                           String email,
                           String phone,
                           String createdBy,
                           String updatedBy,
                           Instant createdDatetime,
                           Instant updatedDatetime) {
}
