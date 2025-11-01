package com.hulkhiretech.payments.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.service.interfaces.PaymentService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService; 

	@PostMapping("/payments/order-create")
	public String createOrder()	{

		log.debug("Received request to create order in PaymentController");
		log.info("Creating order in PayPal order service");
		String response = paymentService.createOrder();
		log.info("Order creation response from service response: {}", response);

		return response;
	}
}
