package com.wings.tender.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.JwtException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidationError(MethodArgumentNotValidException ex){
		Map<String,String> errors = new HashMap<>();
		ex.getAllErrors().forEach(error->{
			errors.put(error.getObjectName(), error.getDefaultMessage());
		});
		
		return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<Object> handleJwtException(JwtException ex){
		return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDeniedEx(AccessDeniedException ex){
		return new ResponseEntity<>(ex.getMessage(),HttpStatus.FORBIDDEN);
	}
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Object> handleIntegrityConstraintViolationEx(DataIntegrityViolationException ex){
		return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Object> handleBadCredsEx(BadCredentialsException ex){
		return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_REQUEST);
	}
}
