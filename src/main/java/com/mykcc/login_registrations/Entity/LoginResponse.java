package com.mykcc.login_registrations.Entity;


import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    // Add other fields for user data if needed
    private Object userData;

    public LoginResponse() {
    }

    public LoginResponse(String message) {
        this.message = message;
    }


}
