package com.ssh.smartServiceHub.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ApiError {

    private Instant timeStamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ApiError() {
        this.timeStamp = Instant.now();
    }

    public ApiError(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
