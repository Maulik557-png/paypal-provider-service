package com.hulkhiretech.payments.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.CreateOrderReq;

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
	 * Validates the CreateOrderReq object.
	 * 
	 * @param createOrderReq the CreateOrderReq object to validate
	 * @throws PaypalProviderException if validation fails
	 */
	public void validateCreateOrderRequest(CreateOrderReq createOrderReq) {
		log.info("Validating CreateOrderReq: {}", createOrderReq);
		
		if(createOrderReq == null) {
			log.error("Trying to validate a null CreateOrderReq object");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
					ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
		if(createOrderReq.getCurrencyCode() == null || createOrderReq.getCurrencyCode().isBlank()) {
			log.error("Currency code is a required field and cannot be null");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_CURRENCY_CODE.getErrorCode(), 
					ErrorCodeEnum.INVALID_CURRENCY_CODE.getErrorMessage(),
					HttpStatus.NOT_FOUND);
		}

		if(createOrderReq.getAmount() == null || createOrderReq.getAmount() <= 0) {
			log.error("Amount must be a valid value greater than zero");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_AMOUNT.getErrorCode(),
					ErrorCodeEnum.INVALID_AMOUNT.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
		if(createOrderReq.getReturnUrl() == null || createOrderReq.getReturnUrl().isBlank()) {
			log.error("Return URL is a required field and cannot be null");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_RETURN_URL.getErrorCode(), 
					ErrorCodeEnum.INVALID_RETURN_URL.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
		if(createOrderReq.getCancelUrl() == null || createOrderReq.getCancelUrl().isBlank()) {
			log.error("Cancel URL is a required field and cannot be null");
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_CANCEL_URL.getErrorCode(),
					ErrorCodeEnum.INVALID_CANCEL_URL.getErrorMessage(),
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
}