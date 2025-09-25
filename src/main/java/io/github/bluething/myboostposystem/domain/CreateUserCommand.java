package io.github.bluething.myboostposystem.domain;

import lombok.Builder;

@Builder
public record CreateUserCommand(String firstName,
                                String lastName,
                                String email,
                                String phone,
                                String createdBy) {
}
