package com.example.pfebtk.demande.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DemandeDejaExisteException.class)
    public ResponseEntity<String> handleDemandeExist(DemandeDejaExisteException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PlusDePlaceException.class)
    public ResponseEntity<String> handleNoPlace(PlusDePlaceException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(SignatureNotDetectedException.class)
    public ResponseEntity<String> handleSignature(SignatureNotDetectedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
