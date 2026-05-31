package br.com.fiap.user.infrastructure.inbound.rest.exceptions.handler;

import br.com.fiap.user.application.domain.exceptions.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private ProblemDetail buildProblemDetailWithMessage(final RuntimeException ex, final HttpStatus status, String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setDetail(message);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    private ProblemDetail buildProblemDetail(final RuntimeException ex, final HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);

        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("One or more fields are invalid.");

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        problemDetail.setProperty("fields", fieldErrors);

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ProblemDetail handleNotFound(final RuntimeException ex) {
        return buildProblemDetail(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateFieldException.class)
    protected ProblemDetail handleConflict(final RuntimeException ex) {
        return buildProblemDetail(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ProblemDetail handleForbidden(final RuntimeException ex) {
        return buildProblemDetail(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(
            {
                    InvalidUserNameException.class,
                    UserOrPasswordInvalidException.class,
                    TokenInvalidException.class,
                    InvalidPasswordException.class
            }
    )
    protected ProblemDetail handleBadRequest(final RuntimeException ex) {
        return buildProblemDetail(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ProblemDetail handleAuthenticationException(final AuthenticationException ex) {
        return buildProblemDetail(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ExternalServiceUnavailableException.class, CallNotPermittedException.class})
    protected ProblemDetail handleServiceUnavailable(final RuntimeException ex) {
        if (ex instanceof ExternalServiceUnavailableException serviceEx) {
            // Se temos status e detail da API externa, usar esses valores
            if (serviceEx.getErrorStatus() != null && serviceEx.getErrorDetail() != null) {
                HttpStatus httpStatus = HttpStatus.valueOf(serviceEx.getErrorStatus());
                return buildProblemDetailWithMessage(
                        serviceEx,
                        httpStatus,
                        serviceEx.getErrorDetail()
                );
            }
            // Caso contrário, usar o status HTTP armazenado
            return buildProblemDetailWithMessage(
                    serviceEx,
                    serviceEx.getHttpStatus(),
                    ex.getMessage()
            );
        }
        return buildProblemDetailWithMessage(
                ex,
                HttpStatus.SERVICE_UNAVAILABLE,
                "Ordered service is currently unavailable. Please try again later."
        );
    }

    @ExceptionHandler(Exception.class)
    protected ProblemDetail handleGeneralException(final RuntimeException ex) {
        String message = "An unexpected error occurred! Please try again later or contact support.";
        return buildProblemDetailWithMessage(ex, HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
