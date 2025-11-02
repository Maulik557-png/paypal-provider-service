package com.hulkhiretech.payments.service.impl;

import java.util.Collections;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.dto.Amount;
import com.hulkhiretech.payments.dto.Breakdown;
import com.hulkhiretech.payments.dto.ExperienceContext;
import com.hulkhiretech.payments.dto.Money;
import com.hulkhiretech.payments.dto.OrderRequest;
import com.hulkhiretech.payments.dto.PaymentSource;
import com.hulkhiretech.payments.dto.Paypal;
import com.hulkhiretech.payments.dto.PurchaseUnit;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final TokenService tokenService;
	
	private final ObjectMapper mapper;
	
	private final HttpServiceEngine httpServiceEngine;
	
	@Override
	public String createOrder() {
		
		/* TODO
		 * 1. getAccessToken (OAuth) - DONE
		 * 2. Call PayPal createOrder
		 * 3. Success/Failure/TimeOut - Proper response handling
		 * 4. What to return to your calling service (payment-processing-service)
		 */
		
		log.debug("Creating order in PaymentServiceImpl");
		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived accessToken: {}", accessToken);

		log.info("Creating order in PaymentServiceImpl");
		// TODO Call PayPal create order API
		
		// Create Money objects
        Money itemTotal = new Money();
        itemTotal.setCurrencyCode("USD");
        itemTotal.setValue("1.00");

        Money shipping = new Money();
        shipping.setCurrencyCode("USD");
        shipping.setValue("5.99");

        Money discount = new Money();
        discount.setCurrencyCode("USD");
        discount.setValue("0.00");

        // Breakdown
        Breakdown breakdown = new Breakdown();
        breakdown.setItemTotal(itemTotal);
        breakdown.setShipping(shipping);
        breakdown.setDiscount(discount);

        // Amount
        Amount amount = new Amount();
        amount.setCurrencyCode("USD");
        amount.setValue("6.99");
        amount.setBreakdown(breakdown);

        // Purchase Unit
        PurchaseUnit purchaseUnit = new PurchaseUnit();
        purchaseUnit.setInvoiceId("INV-1730413562000-3655");
        purchaseUnit.setAmount(amount);

        // Experience Context
        ExperienceContext context = new ExperienceContext();
        context.setPaymentMethodPreference("IMMEDIATE_PAYMENT_REQUIRED");
        context.setLandingPage("LOGIN");
        context.setShippingPreference("NO_SHIPPING");
        context.setUserAction("PAY_NOW");
        context.setReturnUrl("https://example.com/returnUrl");
        context.setCancelUrl("https://example.com/cancelUrl");

        // Payment Source
        Paypal paypal = new Paypal();
        paypal.setExperienceContext(context);

        PaymentSource paymentSource = new PaymentSource();
        paymentSource.setPaypal(paypal);

        // Main Object
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setIntent("CAPTURE");
        orderRequest.setPurchaseUnits(Collections.singletonList(purchaseUnit));
        orderRequest.setPaymentSource(paymentSource);
		
        log.info("OrderRequest object created: {}", orderRequest);
        
        String reqJson = null;
        try {
			reqJson = mapper.writerWithDefaultPrettyPrinter()
			        .writeValueAsString(orderRequest);
			log.info("Create Order Request JSON: {}", reqJson);
		} catch (JsonProcessingException e) {
			log.error("Error while converting OrderRequest to JSON", e);
			throw new RuntimeException("Error while converting OrderRequest to JSON", e);
		}
		
        // Prepare headers
 		HttpHeaders headers = new HttpHeaders();
 		headers.setBearerAuth(accessToken);		
 		headers.setContentType(MediaType.APPLICATION_JSON);

 		String uuid = UUID.randomUUID().toString();
 		headers.add("PayPal-Request-Id", uuid);
 		
 		// Prepare HttpRequest
 		HttpRequest httpRequest = new HttpRequest();
 		httpRequest.setHttpMethod(HttpMethod.POST);
 		httpRequest.setUrl("https://api-m.sandbox.paypal.com/v2/checkout/orders");
 		httpRequest.setHeaders(headers);
 		httpRequest.setBody(reqJson);
        
 		// TODO Make HTTP Call using HttpServiceEngine
 		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP Response from HttpServiceEngine: {}", response);
 		
		log.info("Order created successfully in PaymentServiceImpl");
		
		return response.getBody();
	}
}