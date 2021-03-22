package com.ecommerce;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
		log.info("Handling all Exceptions");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", ex.getMessage());
		body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		body.put("timestamp", LocalDateTime.now());
		log.error("Error occured ", ex);
		return new ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
