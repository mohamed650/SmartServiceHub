package com.ssh.smartServiceHub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private String message;

    public LoginResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

}
