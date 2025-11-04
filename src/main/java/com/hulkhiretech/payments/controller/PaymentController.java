package com.hulkhiretech.payments.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService; 

	@PostMapping("/payments/order-create")
	public OrderResponse createOrder(@RequestBody CreateOrderReq createOrderReq)	{
		log.info("Create order request received in PaymentController||createOrderReq: {}", createOrderReq);	
		
		log.debug("Received request to create order in PaymentController");
		log.info("Creating order in PayPal order service");
		OrderResponse response = paymentService.createOrder(createOrderReq);
		log.info("Order creation response from service response: {}", response);

		return response;
	}
}