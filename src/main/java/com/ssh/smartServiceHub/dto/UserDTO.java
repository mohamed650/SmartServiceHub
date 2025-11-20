package com.ssh.smartServiceHub.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

    private Long id;
    private String fullName;
    private String email;
    private String password;
    private String role;

}
