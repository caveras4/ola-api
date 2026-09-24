package com.caioveras.ola_api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validaDTO(MethodArgumentNotValidException erroValidacao){
        Map<String, String> mensagemCampo = new HashMap<>();

        for(FieldError erro : erroValidacao.getBindingResult().getFieldErrors()){
            mensagemCampo.put(erro.getField(), erro.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(mensagemCampo);
    }
}
