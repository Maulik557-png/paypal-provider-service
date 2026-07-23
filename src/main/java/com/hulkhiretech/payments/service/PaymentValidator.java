package com.hulkhiretech.payments.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentValidator {

	/**
	 * Validates the access token.
	 * 
	 * @param accessToken the access token to validate
	 * @throws PaypalProviderException if validation fails
	 */
	public void validateAccessToken(String accessToken) {
		log.info("Validating access token");
		
		if(accessToken == null || accessToken.isBlank()) {
			log.error("Access token is null or empty");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_ACCESS_TOKEN.getErrorCode(),
					ErrorCodeEnum.INVALID_ACCESS_TOKEN.getErrorMessage(),
					HttpStatus.UNAUTHORIZED);
		}
		if(accessToken.contains(" ")) {
			log.error("Access token contains invalid characters");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_ACCESS_TOKEN.getErrorCode(),
					ErrorCodeEnum.INVALID_ACCESS_TOKEN.getErrorMessage(),
					HttpStatus.UNAUTHORIZED);
		}
	}
	
	/**
	 * Validates the create order request.
	 * 
	 * @param createOrderReq the CreateOrderReq to validate
	 * @param httpRequest    the HttpRequest to validate
	 * @throws PaypalProviderException if validation fails
	 */
	public void validateCreateOrderRequest(CreateOrderReq createOrderReq, HttpRequest httpRequest) {
	    log.info("Validating CreateOrderReq: {}", createOrderReq);

	    Field[] fields = createOrderReq.getClass().getDeclaredFields();
	    Arrays.sort(fields, Comparator.comparing(Field::getName));
	    
	    validateObjectNotNull(createOrderReq, "CreateOrderReq");
	    validateAmount(createOrderReq.getAmount(), ErrorCodeEnum.INVALID_AMOUNT, fields[0].getName());
	    validateField(createOrderReq.getCancelUrl(), ErrorCodeEnum.INVALID_CANCEL_URL, fields[1].getName());
	    validateField(createOrderReq.getCurrencyCode(), ErrorCodeEnum.INVALID_CURRENCY_CODE, fields[2].getName());	
	    validateField(createOrderReq.getReturnUrl(), ErrorCodeEnum.INVALID_RETURN_URL, fields[3].getName());

	    validateObjectNotNull(httpRequest, "HttpRequest");
	    validateHttpRequest(httpRequest);
	}
	
	/**
	 * Validates the HttpRequest.
	 * 
	 * @param httpRequest the HttpRequest to validate
	 * @throws PaypalProviderException if validation fails
	 */
	private void validateHttpRequest(HttpRequest httpRequest) {
	    // body
	    if (httpRequest.getBody() == null) {
	        log.error("HttpRequest body is null");
	        throw new PaypalProviderException(
	                ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
	                ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
	                HttpStatus.BAD_REQUEST);
	    }

	    // headers
	    if (httpRequest.getHeaders() == null || httpRequest.getHeaders().isEmpty()) {
	        log.error("HttpRequest headers are null or empty");
	        throw new PaypalProviderException(
	                ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
	                ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
	                HttpStatus.BAD_REQUEST);
	    }

	    // url
	    validateField(httpRequest.getUrl(), ErrorCodeEnum.INVALID_REQUEST, "HttpRequest URL");

	    // http method
	    validateObjectNotNull(httpRequest.getHttpMethod(), "HttpRequest HTTP method");
	}
	
	/**
	 * Validates that an object is not null.
	 * 
	 * @param obj       the object to validate
	 * @param fieldName the name of the field being validated
	 * @throws PaypalProviderException if the object is null
	 */
	private void validateObjectNotNull(Object obj, String fieldName) {
	    if (obj == null) {
	        log.error("{} is null", fieldName);
	        throw new PaypalProviderException(
	                ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
	                ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
	                HttpStatus.BAD_REQUEST);
	    }
	}
	
	/**
	 * Validates a string field.
	 * 
	 * @param fieldValue    the value of the field to validate
	 * @param errorCodeEnum the error code enum to use for exceptions
	 * @param fieldName     the name of the field being validated
	 * @throws PaypalProviderException if the field is null or blank
	 */
	private void validateField(String fieldValue, ErrorCodeEnum errorCodeEnum, String fieldName) {
	    if (fieldValue == null || fieldValue.isBlank()) {
	        log.error("{} is a required field and cannot be null or blank", fieldName);
	        throw new PaypalProviderException(
	                errorCodeEnum.getErrorCode(),
	                errorCodeEnum.getErrorMessage(),
	                HttpStatus.BAD_REQUEST);
	    }
	}

	/**
	 * Validates the amount.
	 * 
	 * @param amount the amount to validate
	 * @throws PaypalProviderException if the amount is null or less than or equal to zero
	 */
	private void validateAmount(Double amount, ErrorCodeEnum errorCodeEnum, String fieldName) {
	    if (amount == null || amount <= 0) {
	        log.error("{} must be a valid value greater than zero", fieldName);
	        throw new PaypalProviderException(
	                errorCodeEnum.getErrorCode(),
	                errorCodeEnum.getErrorMessage(),
	                HttpStatus.BAD_REQUEST);
	    }
	}

	
	/**
	 * Validates the PayPal order response.
	 * 
	 * @param paypalOrder the PayPal order response to validate
	 * @return true if the response is valid, false otherwise
	 */
	public boolean validateCreateOrderResponse(PaypalOrder paypalOrder) {
		log.info("Validating OrderResponse: {}", paypalOrder);
		
		return paypalOrder != null
				&& paypalOrder.getId() != null
				&& !paypalOrder.getId().isBlank()
				&& Constant.PAYER_ACTION_REQUIRED.equalsIgnoreCase(paypalOrder.getStatus())
				&& paypalOrder.getLinks() != null
				&& !paypalOrder.getLinks().isEmpty();
	}
	
	/**
	 * Validates the capture order request.
	 * 
	 * @param orderId the order ID to validate
	 * @throws PaypalProviderException if validation fails
	 */
	public void validateCaptureOrderRequest(String orderId) {
		log.info("Validating capture order request for orderId: {}", orderId);
		
		if(orderId == null || orderId.isBlank()) {
			log.error("Order ID is a required field and cannot be null");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_ORDER_ID.getErrorCode(),
					ErrorCodeEnum.INVALID_ORDER_ID.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Validates the capture order response.
	 * 
	 * @param orderResponse the OrderResponse to validate
	 * @return true if the response is valid, false otherwise
	 */
	public boolean validateCaptureOrderResponse(OrderResponse orderResponse) {
		log.info("Validating capture order response: {}", orderResponse);
		
		return orderResponse != null 
				&& orderResponse.getOrderId() != null
				&& !orderResponse.getOrderId().isEmpty()
				&& orderResponse.getPaypalStatus() != null
				&& !orderResponse.getPaypalStatus().isEmpty()
				&& orderResponse.getPaypalStatus().equalsIgnoreCase(Constant.COMPLETED);
	}
}