package com.example.productapi.exception;

public class ErrorDetails {
    private String message;
    private String details;

    //Constructor
    public ErrorDetails(String message, String details) {
        this.message = message;
        this.details = details;
    }

    //Getters
    public String getMessage() {
        return message;
    }
    public String getDetails() {
        return details;
    }

    //Setters
    public void setMessage(String message) {
        this.message = message;
    }
    public void setDetails(String details) {
        this.details = details;
    }
}
