package br.com.fiap.user.application.domain.exceptions;

import org.springframework.http.HttpStatus;

public class ExternalServiceUnavailableException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorDetail;
    private final Integer errorStatus;

    public ExternalServiceUnavailableException(String message) {
        super(message, null, true, false);
        this.httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        this.errorDetail = null;
        this.errorStatus = null;
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause, true, false);
        this.httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        this.errorDetail = null;
        this.errorStatus = null;
    }

    public ExternalServiceUnavailableException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause, true, false);
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.SERVICE_UNAVAILABLE;
        this.errorDetail = null;
        this.errorStatus = null;
    }

    public ExternalServiceUnavailableException(String message, Throwable cause, HttpStatus httpStatus, String errorDetail, Integer errorStatus) {
        super(message, cause, true, false);
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.SERVICE_UNAVAILABLE;
        this.errorDetail = errorDetail;
        this.errorStatus = errorStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public Integer getErrorStatus() {
        return errorStatus;
    }
}
