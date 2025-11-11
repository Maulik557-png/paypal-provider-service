package com.hulkhiretech.payments.service.impl;

import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.CaptureOrderService;
import com.hulkhiretech.payments.service.CreateOrderService;
import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final TokenService tokenService;
	
	private final CreateOrderService createOrderService;
	
	private final CaptureOrderService captureOrderService;
	
	@Override
	public OrderResponse createOrder(CreateOrderReq createOrderReq) {
		log.debug("Creating order in PaymentServiceImpl||createOrderReq: {}", createOrderReq);
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived: {}", accessToken);

        OrderResponse orderResponse = createOrderService.createOrder(createOrderReq, accessToken);
		log.info("OrderResponse received from createOrder method: {}", orderResponse);
        
		// TODO TimeOut handling using Circuit Breaker pattern 
		
		log.info("Order created successfully in PaymentServiceImpl");
		return orderResponse;
	}
	
	@Override
	public String captureOrder(String orderId) {
		log.info("Capturing order in PaymentServiceImpl||orderId: {}", orderId);
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived for capture order: {}", accessToken);
		
		String orderDetails = captureOrderService.showOrder(orderId, accessToken);
		log.info("Order details retrieved using showOrder: {}", orderDetails);
		
		if(!"APPROVED".equalsIgnoreCase(orderDetails)) {
			log.warn("Order is not in APPROVED status, cannot capture order. Current status/details: {}", orderDetails);
			return orderDetails;
		}
		
		String captureResponse = captureOrderService.captureOrder(orderId, accessToken);
		log.info("Order captured successfully in PaymentServiceImpl||captureResponse: {}", captureResponse);
		
		return captureResponse;
	}
		
}