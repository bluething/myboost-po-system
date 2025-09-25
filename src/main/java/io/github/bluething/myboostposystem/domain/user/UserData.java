package io.github.bluething.myboostposystem.domain.user;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserData(Integer id,
                       String firstName,
                       String lastName,
                       String email,
                       String phone,
                       String createdBy,
                       String updatedBy,
                       Instant createdDatetime,
                       Instant updatedDatetime) {
}
