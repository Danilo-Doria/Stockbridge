package com.stockbridge.inventory.infrastructure.adapter.in.rest.exception;

import com.stockbridge.inventory.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail notFound(ProductNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler({DuplicateProductCodeException.class, InsufficientStockException.class})
    ProblemDetail conflict(DomainException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto con el estado actual", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ProblemDetail emailTaken(EmailAlreadyRegisteredException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto con el estado actual", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail badCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)           // cualquier otra regla de negocio
    ProblemDetail businessRule(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio violada", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no cumplen las validaciones");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Error inesperado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
    }

    @ExceptionHandler(AccessDeniedException.class)       // org.springframework.security.access
    ProblemDetail denied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", "No tienes permisos para este recurso");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Cuerpo inválido", "El JSON de la petición está mal formado");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
