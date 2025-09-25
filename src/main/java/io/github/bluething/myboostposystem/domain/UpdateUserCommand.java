package io.github.bluething.myboostposystem.domain;

import lombok.Builder;

@Builder
public record UpdateUserCommand(String firstName,
                                String lastName,
                                String email,
                                String phone,
                                String updatedBy) {
}
