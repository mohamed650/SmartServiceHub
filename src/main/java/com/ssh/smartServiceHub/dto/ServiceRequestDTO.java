package com.ssh.smartServiceHub.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceRequestDTO {

    private Long id;
    private String title;
    private String description;
    private String status;

    private Long userId;

    private Long categoryId;

}
