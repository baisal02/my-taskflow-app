package myproject.taskflow.dto.response;

import org.springframework.http.HttpStatus;

public class SimpleResponse {
    private String message;
    private HttpStatus status;

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
    public SimpleResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
