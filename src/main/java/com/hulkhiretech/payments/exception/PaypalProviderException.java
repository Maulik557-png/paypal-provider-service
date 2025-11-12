package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Custom exception class for PayPal provider errors.
 */
@Getter
public class PaypalProviderException extends RuntimeException {

	private static final long serialVersionUID = -4440680443001298272L;
	
	private final String errorCode;
	private final String errorMessage;
	
	private final HttpStatus httpStatus;
	
	public PaypalProviderException(String errorCode, String errorMessage, HttpStatus httpStatus) {
		super(errorMessage);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.httpStatus = httpStatus;
	}
}
