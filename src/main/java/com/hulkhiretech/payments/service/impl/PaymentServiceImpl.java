package com.hulkhiretech.payments.service.impl;


import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
	
	private final TokenService tokenService;
	
	@Override
	public String createOrder() {
		
		log.info("Creating order in PaymentServiceImpl");
		
		/* TODO
		 * 1. getAccessToken (OAuth) - DONE
		 * 2. Call PayPal createOrder
		 * 3. Success/Failure/TimeOut - Proper response handling
		 * 4. What to return to your calling service (payment-processing-service)
		 */
		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived accessToken: {}", accessToken);

		log.info("Creating order in PaymentServiceImpl");
		
		log.info("Order created successfully in PaymentServiceImpl");
		
		return "Order Created from service - " + accessToken;
	}

	@PostConstruct
	public void init()	{
		log.info("Initializing PaymentServiecImpl");
	}

}