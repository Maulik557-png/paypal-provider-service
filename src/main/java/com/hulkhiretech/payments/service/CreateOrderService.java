package com.hulkhiretech.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.helper.PaypalRequestBuilder;
import com.hulkhiretech.payments.service.helper.PaypalResponseMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class to handle order creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderService {

	private final PaymentValidator paymentValidator;
	
	private final HttpServiceEngine httpServiceEngine;

	private final PaypalRequestBuilder paypalRequestBuilder;

	private final PaypalResponseMapper paypalResponseMapper;
	
	/**
	 * Creates an order with the given CreateOrderReq and access token.
	 * 
	 * @param createOrderReq the CreateOrderReq object containing order details
	 * @param accessToken    the access token for authentication
	 * @return the OrderResponse containing order details
	 */
	public OrderResponse createOrder(CreateOrderReq createOrderReq, String accessToken) {
		log.info("CreateOrderService createOrder method called with CreateOrderReq: {}", createOrderReq);
		
		paymentValidator.validateCreateOrderRequest(createOrderReq);
		log.info("CreateOrderReq validated successfully: {}", createOrderReq);
		
		HttpRequest httpRequest = paypalRequestBuilder.prepareCreateOrderRequest(createOrderReq, accessToken);
		log.info("Create Order HttpRequest prepared: {}", httpRequest);

		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP Response from HttpServiceEngine: {}", response);
		
		OrderResponse orderResponse = paypalResponseMapper.prepareOrderResponse(response);
		log.info("OrderResponse prepared from PaypalResponseMapper: {}", orderResponse);
		return orderResponse;
	}
}
