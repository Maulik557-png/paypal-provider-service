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
	 * Converts a PaypalOrder to an OrderResponse.
	 *
	 * @param paypalOrder the PayPal order
	 * @return the corresponding OrderResponse
	 */
	public OrderResponse toCreateOrderResponse(PaypalOrder paypalOrder) {
		log.info("Converting PaypalOrder to OrderResponse: {}", paypalOrder);

		OrderResponse response = new OrderResponse();
		response.setOrderId(paypalOrder.getId());
		response.setPaypalStatus(paypalOrder.getStatus());

		String redirectUrl = paypalOrder.getLinks().stream()
				.filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
				.map(PaypalLink::getHref)
				.findFirst()
				.orElse(null);

		response.setRedirectUrl(redirectUrl);

		log.info("Converted PaypalOrder to OrderResponse: {}", response);
		return response;
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

			OrderResponse orderResponse = toCreateOrderResponse(paypalOrder);
			log.info("Converted OrderResponse: {}", orderResponse);

			if(paymentValidator.validateCreateOrderResponse(paypalOrder)) {
				return orderResponse;
			} 
			
			log.error("Order creation failed or incomplete details received. " + "orderResponse: {}", orderResponse);
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
	
	/**
	 * Prepares an OrderResponse for completed payment status.
	 *
	 * @param orderStatus the PayPal order details
	 * @return the prepared OrderResponse indicating completed payment
	 */
	public OrderResponse completedPaymentResponse(PaypalOrder orderStatus) {
		log.info("Evaluating PayPal order status: {}", orderStatus);

		log.info("Order is already CAPTURED. Current status: {}", orderStatus);
		OrderResponse alreadyCapturedResponse = new OrderResponse();
		alreadyCapturedResponse.setOrderId(orderStatus.getId());
		alreadyCapturedResponse.setPaypalStatus(orderStatus.getStatus());
		log.info("Order is in COMPLETED status. Returning completed payment response: {}", alreadyCapturedResponse);
		return alreadyCapturedResponse;
	}
	
	/**
	 * Prepares an OrderResponse for pending payment status.
	 *
	 * @param orderStatus the PayPal order details
	 * @return the prepared OrderResponse indicating pending payment
	 */
	public OrderResponse pendingPaymentResponse(PaypalOrder orderStatus) {
		log.info("Evaluating PayPal order status: {}", orderStatus);

		log.warn("Order status is not APPROVED/COMPLETED. Current status: {}", orderStatus);
		OrderResponse paymentPendingResponse = new OrderResponse();
		paymentPendingResponse.setOrderId(orderStatus.getId());
		paymentPendingResponse.setPaypalStatus(orderStatus.getStatus());
		paymentPendingResponse.setRedirectUrl(
				orderStatus.getLinks().stream()
				.filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
				.map(PaypalLink::getHref)
				.findFirst()
				.orElse(null));
		log.info("Order is not in APPROVED/COMPLETED status. Returning pending payment response: {}", paymentPendingResponse);
		return paymentPendingResponse;
	}
	
	/**
	 * Converts a PaypalOrder to an OrderResponse.
	 *
	 * @param paypalOrder the PayPal order
	 * @return the corresponding OrderResponse
	 */
	public OrderResponse toCaptureOrderResponse(PaypalOrder paypalOrder) {
		log.info("Converting PaypalOrder to OrderResponse: {}", paypalOrder);

		OrderResponse response = new OrderResponse();
		response.setOrderId(paypalOrder.getId());
		response.setPaypalStatus(paypalOrder.getStatus());

		log.info("Converted PaypalOrder to OrderResponse: {}", response);
		return response;
	}
	
	/**
	 * Handles the capture response from PayPal and converts it to an OrderResponse.
	 *
	 * @param httpResponse the HTTP response from PayPal
	 * @return the corresponding OrderResponse
	 * @throws PaypalProviderException if the response indicates an error
	 */
	public OrderResponse handleCaptureResponse(ResponseEntity<String> httpResponse) {
		log.info("Handling PayPal response in PaymentServiceImpl "
				+ "httpResponse:{}", httpResponse);

		if(httpResponse.getStatusCode().is2xxSuccessful()) { //success

			PaypalOrder paypalOrder = jsonUtil.fromJson(httpResponse.getBody(), PaypalOrder.class);
			log.info("Converted response body to PaypalOrder: {}", paypalOrder);

			OrderResponse orderResponse = toCaptureOrderResponse(paypalOrder);
			log.info("Converted OrderResponse: {}", orderResponse);

			if(paymentValidator.validateCaptureOrderResponse(orderResponse)) {
				return orderResponse;
			}

			log.error("Order creation failed or incomplete details received. " + "orderResponse: {}", orderResponse);
		}

		// if 4xx or 5xx then proper error
		if(httpResponse.getStatusCode().is4xxClientError() || httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Received 4xx, 5xx error response from PayPal service");

			PaypalErrorResponse paypalErrorRes = jsonUtil.fromJson(httpResponse.getBody(), PaypalErrorResponse.class);
			log.info("PayPal error response details: {}", paypalErrorRes);

			String errorCode = ErrorCodeEnum.PAYPAL_ERROR.getErrorCode();
			String errorMessage = PaypalErrorUtil.getResponseSummary(paypalErrorRes);
			log.info("Generated PayPal error summary: {}", errorMessage);

			throw new PaypalProviderException(errorCode, errorMessage, HttpStatus.valueOf(httpResponse.getStatusCode().value()));
		}

		log.error("Unexpected response from PayPal service. " + "httpResponse: {}", httpResponse);

		throw new PaypalProviderException(
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorCode(),
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorMessage(),
				HttpStatus.BAD_GATEWAY);
	}
}