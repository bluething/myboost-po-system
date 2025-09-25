package io.github.bluething.myboostposystem.rest.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreateUserRequest(@NotBlank(message = "First name is required")
                         @Size(max = 500, message = "First name must not exceed 500 characters")
                         String firstName,

                         @NotBlank(message = "Last name is required")
                         @Size(max = 500, message = "Last name must not exceed 500 characters")
                         String lastName,

                         @NotBlank(message = "Email is required")
                         @Email(message = "Email must be valid")
                         String email,

                         @Size(max = 20, message = "Phone must not exceed 20 characters")
                         String phone) {
}
