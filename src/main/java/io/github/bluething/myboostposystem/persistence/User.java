package io.github.bluething.myboostposystem.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_created_datetime", columnList = "created_datetime"),
        @Index(name = "idx_users_full_name", columnList = "first_name, last_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "id")
@ToString(exclude = {"createdDatetime", "updatedDatetime"})
class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "First name is required")
    @Size(max = 500, message = "First name must not exceed 500 characters")
    @Column(name = "first_name", length = 500, nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 500, message = "Last name must not exceed 500 characters")
    @Column(name = "last_name", length = 500, nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Returns full name of the user
     * @return concatenated first name and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Pre-persist callback to set audit fields
     */
    @PrePersist
    protected void onCreate() {
        if (getCreatedBy() == null) {
            setCreatedBy("system"); // Default value, should be set by service
        }
    }

    /**
     * Pre-update callback to set audit fields
     */
    @PreUpdate
    protected void onUpdate() {
        if (getUpdatedBy() == null) {
            setUpdatedBy("system"); // Default value, should be set by service
        }
    }
}
