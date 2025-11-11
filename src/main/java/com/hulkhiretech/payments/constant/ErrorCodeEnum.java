package com.hulkhiretech.payments.constant;

import lombok.Getter;

/**
 * Enum representing various error codes and their corresponding messages.
 */
@Getter
public enum ErrorCodeEnum {
	
	GENERAL_ERROR("30000", "Something went wrong. Please try again later."),
	INVALID_REQUEST("30001", "The request is invalid. Please check the request parameters."), 
	INVALID_CURRENCY_CODE("30002", "Currency code is a required field and cannot be null"), 
	INVALID_AMOUNT("30003", "Amount must be a valid value greater than zero"),
	INVALID_RETURN_URL("30004", "Return URL is a required field and cannot be null"), 
	INVALID_CANCEL_URL("30005", "Cancel URL is a required field and cannot be null"), 
	INVALID_ACCESS_TOKEN("30006", "The access token provided is invalid or has expired."), 
	PAYPAL_SERVICE_UNAVAILABLE("30007", "Paypal service is currently unavailable. Please try again later."), 
	PAYPAL_ERROR("30008", "<Error from Paypal>: %s"),
	PAYPAL_UNKNOWN_ERROR("30009", "An unknown error occurred while processing the Paypal response"),
	PAYER_ACTION_REQUIRED("30010", "Payer action is required to complete the payment.");
	
	private final String errorCode;
	private final String errorMessage;	
	
	private ErrorCodeEnum(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}
