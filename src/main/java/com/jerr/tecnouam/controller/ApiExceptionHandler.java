package com.jerr.tecnouam.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice(assignableTypes = {EquipoController.class, PrestamoController.class})
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail negocio(ResponseStatusException excepcion) {
        return ProblemDetail.forStatusAndDetail(excepcion.getStatusCode(), excepcion.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException excepcion) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Los datos enviados no son validos");
        List<String> errores = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail validacionServicio(ConstraintViolationException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "JSON invalido: revise tipos, valores de enumeraciones y fechas (AAAA-MM-DD)");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La operacion entra en conflicto con los datos existentes; revise el codigo patrimonial y las relaciones");
    }
}
