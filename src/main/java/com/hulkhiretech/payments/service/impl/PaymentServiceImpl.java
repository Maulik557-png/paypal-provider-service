package com.hulkhiretech.payments.service.impl;

import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
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
	
	@Override
	public OrderResponse createOrder(CreateOrderReq createOrderReq) {
		log.debug("Creating order in PaymentServiceImpl||createOrderReq: {}", createOrderReq);
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived: {}", accessToken);

        OrderResponse orderResponse = createOrderService.createOrder(createOrderReq, accessToken);
		log.info("OrderResponse received from createOrder method: {}", orderResponse);
        
		// Success/Failure/TimeOut - Proper response handling
		
		log.info("Order created successfully in PaymentServiceImpl");
		return orderResponse;
	}
}