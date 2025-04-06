package dev.gyeoul.esginsightboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String name;
    private String email;
    private String password;        // optional
    private String phoneNumber;
}
