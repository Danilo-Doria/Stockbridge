package com.stockbridge.inventory.infrastructure.adapter.in.rest.exception;

import com.stockbridge.inventory.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ---------- 404: recurso no encontrado ----------
    @ExceptionHandler({ProductNotFoundException.class, RequestNotFoundException.class,
            UserNotFoundException.class})
    ProblemDetail notFound(DomainException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    // ---------- 409: conflicto con el estado actual ----------
    @ExceptionHandler({DuplicateProductCodeException.class, InsufficientStockException.class,
            InvalidStateTransitionException.class, EmailAlreadyRegisteredException.class})
    ProblemDetail conflict(DomainException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto con el estado actual", ex.getMessage());
    }

    // ---------- 401: autenticación ----------
    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail badCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    // ---------- 403: autorización ----------
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail denied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para este recurso");
    }

    // ---------- 422: cualquier otra regla de negocio ----------
    @ExceptionHandler(DomainException.class)
    ProblemDetail businessRule(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio violada", ex.getMessage());
    }

    // ---------- 503: integración externa ----------
    @ExceptionHandler(SupplierUnavailableException.class)
    ProblemDetail supplierDown(SupplierUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Proveedor no disponible",
                "No se pudo consultar la disponibilidad externa. Intenta más tarde.");
    }

    // ---------- 400: parámetro de ruta o query con formato inválido ----------
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail badParam(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El valor de '" + ex.getName() + "' no tiene el formato esperado");
    }

    // ---------- 500: lo verdaderamente inesperado ----------
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Error inesperado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado");
    }

    // ---------- 400: Bean Validation del body (con detalle por campo) ----------
    // JSON mal formado, método no permitido, etc. ya los maneja la clase base.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no cumplen las validaciones");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}