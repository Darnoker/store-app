package com.github.darnoker.userservice.api;

import com.github.darnoker.userservice.identity.AuthenticationService.InvalidCredentialsException;
import com.github.darnoker.userservice.user.UserService.DuplicateEmailException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({InvalidCredentialsException.class})
    ResponseEntity<Void> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<Void> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
