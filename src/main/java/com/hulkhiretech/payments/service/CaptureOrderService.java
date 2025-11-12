package com.hulkhiretech.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.helper.PaypalRequestBuilder;
import com.hulkhiretech.payments.service.helper.PaypalResponseMapper;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptureOrderService {

	private final JsonUtil jsonUtil;

	private final PaymentValidator paymentValidator;

	private final HttpServiceEngine httpServiceEngine;

	private final PaypalRequestBuilder paypalRequestBuilder;

	private final PaypalResponseMapper paypalResponseMapper;

	/**
	 * Shows the order details for the given orderId using the provided access token.
	 * 
	 * @param orderId     the ID of the order to be shown
	 * @param accessToken the access token for authentication
	 * @return the PaypalOrderDetails containing order information
	 */
	public PaypalOrder showOrder(String orderId, String accessToken) {
		log.info("Showing order in CaptureOrderService||orderId: {}, accessToken: {}", orderId, accessToken);

		HttpRequest httpRequest = paypalRequestBuilder.prepareShowOrderRequest(orderId, accessToken);
		log.info("Show Order HttpRequest prepared: {}", httpRequest);

		ResponseEntity<String> showOrderResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("Show order response received: {}", showOrderResponse);

		PaypalOrder orderDetails = jsonUtil.fromJson(showOrderResponse.getBody(), PaypalOrder.class);
		log.info("PaypalShowOrder object parsed from response: {}", orderDetails);

		log.info("Paypal order status for orderId {}: {}", orderId, orderDetails.getStatus());
		return orderDetails;
	}

	/**
	 * Captures the order for the given orderId using the provided access token.
	 * 
	 * @param orderId     the ID of the order to be captured
	 * @param accessToken the access token for authentication
	 * @return the OrderResponse containing captured order details
	 */
	public OrderResponse captureOrder(String orderId, String accessToken) {
		log.info("Capturing order in CaptureOrderService||orderId: {}, accessToken: {}", orderId, accessToken);

		paymentValidator.validateCaptureOrderRequest(orderId);
		log.info("Capture order request validated successfully for orderId: {}", orderId);

		HttpRequest httpRequest = paypalRequestBuilder.prepareCaptureOrderRequest(orderId, accessToken);
		log.info("Capture Order HttpRequest prepared: {}", httpRequest);

		ResponseEntity<String> captureResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("Capture order response received: {}", captureResponse);

		OrderResponse orderResponse = paypalResponseMapper.handleCaptureResponse(captureResponse);
		log.info("OrderResponse prepared from PaypalResponseMapper: {}", orderResponse); 		

		return orderResponse;
	}
}
