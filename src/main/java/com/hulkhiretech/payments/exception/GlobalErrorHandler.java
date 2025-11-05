package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.pojo.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {
	
	@ExceptionHandler(PaypalProviderException.class)
	public ResponseEntity<ErrorResponse> handlePaypalProviderException(PaypalProviderException ex) {
		log.error("Handling PaypalProviderException - ErrorCode: {}, ErrorMessage: {}", ex.getErrorCode(), ex.getErrorMessage());
		
		ErrorResponse errorResponse = new ErrorResponse(
				ex.getErrorCode(),
				ex.getErrorMessage()
		);

		return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Handling General Exception: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse(
				ErrorCodeEnum.GENERAL_ERROR.getErrorCode(),
				ErrorCodeEnum.GENERAL_ERROR.getErrorMessage()
		);

		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
