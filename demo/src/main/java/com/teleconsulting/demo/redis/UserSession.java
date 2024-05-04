package com.teleconsulting.demo.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSession {
    private String username;
    private String ipAddress;
    private String accessToken;
    private long expirationTimeMillis;
}
