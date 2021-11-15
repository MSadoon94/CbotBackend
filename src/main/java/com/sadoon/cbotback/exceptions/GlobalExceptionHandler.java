package com.sadoon.cbotback.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        logException(apiError);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFoundException(CustomException ex) {
        return buildResponseEntity(addSubErrors(buildApiError(HttpStatus.NOT_FOUND, ex), ex));
    }

    @ExceptionHandler(CardPasswordEncryptionException.class)
    protected ResponseEntity<Object> handleCardPasswordEncryptionException(CardPasswordEncryptionException ex) {
        return buildResponseEntity(addSubErrors(buildApiError(HttpStatus.PRECONDITION_FAILED, ex), ex));
    }

    @ExceptionHandler(WebClientResponseException.class)
    protected ResponseEntity<Object> handleWebClientResponseException(WebClientResponseException ex) {
        ApiError apiError = new ApiError(ex.getStatusCode());
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(KrakenRequestException.class)
    protected ResponseEntity<Object> handleKrakenRequestException(KrakenRequestException ex) {
        return buildResponseEntity(addSubErrors(buildApiError(HttpStatus.BAD_REQUEST, ex), ex));
    }

    @ExceptionHandler({UnauthorizedUserException.class, CredentialsException.class})
    protected ResponseEntity<Object> handleUnauthorizedUser(CustomException ex) {
        return buildResponseEntity(addSubErrors(buildApiError(HttpStatus.UNAUTHORIZED, ex), ex));
    }

    private ApiError buildApiError(HttpStatus status, CustomException ex) {
        ApiError apiError = new ApiError(status);
        apiError.setMessage(ex.getMessage());
        return apiError;
    }

    private void logException(ApiError error) {
        logger.error(error.getMessage(), error);
    }

    private ApiError addSubErrors(ApiError error, CustomException ex) {
        if (ex.getSubError().isPresent()) {
            error.setSubErrors(List.of(ex.getSubError().get()));
        }
        return error;
    }
}
