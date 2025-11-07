package com.hulkhiretech.payments.service.helper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.paypal.res.PaypalLink;
import com.hulkhiretech.payments.paypal.res.PaypalOAuthToken;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.paypal.res.error.PaypalErrorResponse;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.PaymentValidator;
import com.hulkhiretech.payments.util.JsonUtil;
import com.hulkhiretech.payments.util.PaypalErrorUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper class to convert PayPal API responses into application-specific objects.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaypalResponseMapper {

	private final JsonUtil jsonUtil;

	private final PaymentValidator paymentValidator;
	
	/**
	 * Prepares a PaypalOAuthToken from the given PayPal response.
	 *
	 * @param response the PayPal response entity
	 * @return the prepared PaypalOAuthToken
	 * @throws PaypalProviderException if the response indicates an error
	 */
	public PaypalOAuthToken prepareTokenResponse(ResponseEntity<String> response) {
		log.debug("Preparing PaypalOAuthToken from Paypal response||response: {}", response);
		
		// if 2xx response from PayPal
		if(response.getStatusCode().is2xxSuccessful()) {
			log.info("Successful response received from Paypal for OAuth token");
			return jsonUtil.fromJson(response.getBody(), PaypalOAuthToken.class);
		}

		// Everything else is treated as failure
		log.error("Non-successful response received from Paypal for OAuth token: Status Code: {}, Body: {}",
				response.getStatusCode(), response.getBody());
		throw new PaypalProviderException(
				"Error retrieving OAuth token from Paypal",
				"Failed to retrieve OAuth token from Paypal",	
				HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	/**
	 * Prepares an OrderResponse from the given PayPal response.
	 *
	 * @param response the PayPal response entity
	 * @return the prepared OrderResponse
	 * @throws PaypalProviderException if the response indicates an error
	 */
	public OrderResponse prepareOrderResponse(ResponseEntity<String> response)	{
		log.debug("Preparing OrderResponse from Paypal response||response: {}", response);

		PaypalOrder paypalOrder = null;
		// if 2xx response from PayPal
		if(response.getStatusCode().is2xxSuccessful()) {
			log.info("Successful response received from Paypal for Order creation");

			paypalOrder = jsonUtil.fromJson(response.getBody(), PaypalOrder.class);
			log.info("PaypalOrder object created: {}", paypalOrder);

			if(paymentValidator.validateCreateOrderResponse(paypalOrder)) {
				OrderResponse orderResponse = new OrderResponse();
				orderResponse.setOrderId(paypalOrder.getId());
				orderResponse.setStatus(paypalOrder.getStatus());

				String redirectUrl = paypalOrder.getLinks().stream()
						.filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
						.map(PaypalLink::getHref)
						.findFirst()
						.orElse(null);

				orderResponse.setRedirectUrl(redirectUrl);

				return orderResponse;
			} 
		}
		
		// if 4xx or 5xx response from PayPal
		if(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
			log.error("4xx/5xx error response received from Paypal for Order creation: Status Code: {}, Body: {}",
					response.getStatusCode(), response.getBody());
			
			PaypalErrorResponse paypalErrorRes = jsonUtil.fromJson(response.getBody(), PaypalErrorResponse.class);
			log.info("PaypalErrorResponse object created: {}", paypalErrorRes);
			
			String errorMessage = PaypalErrorUtil.getResponseSummary(paypalErrorRes);
			
			throw new PaypalProviderException(
					ErrorCodeEnum.PAYPAL_ERROR.getErrorCode(),
					errorMessage,	
					HttpStatus.valueOf(response.getStatusCode().value()));
		}
		
		// Everything else is treated as failure
		log.error("Non-successful response received from Paypal for Order creation: Status Code: {}, Body: {}",
				response.getStatusCode(), response.getBody());

		throw new PaypalProviderException(
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorCode(),
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorMessage(),	
				HttpStatus.BAD_GATEWAY);
	}
}