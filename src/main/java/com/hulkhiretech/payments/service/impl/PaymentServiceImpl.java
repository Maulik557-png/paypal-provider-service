package com.hulkhiretech.payments.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.dto.Amount;
import com.hulkhiretech.payments.dto.ExperienceContext;
import com.hulkhiretech.payments.dto.OrderRequest;
import com.hulkhiretech.payments.dto.PaymentSource;
import com.hulkhiretech.payments.dto.Paypal;
import com.hulkhiretech.payments.dto.PurchaseUnit;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
	
	@Value("${paypal.create.order.url}")
	private String createOrderUrl;

	private final TokenService tokenService;
	
	private final ObjectMapper mapper;
	
	private final HttpServiceEngine httpServiceEngine;
	
	@Override
	public String createOrder(CreateOrderReq createOrderReq) {
		log.debug("Creating order in PaymentServiceImpl||createOrderReq: {}", createOrderReq);
		
		/* TODO
		 * 1. getAccessToken (OAuth) - DONE
		 * 2. Call PayPal createOrder
		 * 3. Success/Failure/TimeOut - Proper response handling
		 * 4. What to return to your calling service (payment-processing-service)
		 */
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived accessToken: {}", accessToken);

		log.info("Creating order in PaymentServiceImpl");

        // Amount
        Amount amount = new Amount();
        amount.setCurrencyCode(createOrderReq.getCurrencyCode());
		amount.setValue(String.format(Constant.TWO_DECIMAL_FORMAT, createOrderReq.getAmount()));

        // Purchase Unit
        PurchaseUnit purchaseUnit = new PurchaseUnit();
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        String invoiceId = "INV-" + timestamp + "-" + randomNum;
        purchaseUnit.setInvoiceId(invoiceId);
        purchaseUnit.setAmount(amount);

        // Experience Context
        ExperienceContext context = new ExperienceContext();
        context.setPaymentMethodPreference(Constant.PAYMENT_PREFERENEC_IMMEDIATE_PAYMENT_REQUIRED);
        context.setLandingPage(Constant.LANDING_PAGE_LOGIN);
        context.setShippingPreference(Constant.SHIPPING_PREFERENCE_NO_SHIPPING);
        context.setUserAction(Constant.USER_ACTION_PAY_NOW);
        context.setReturnUrl(createOrderReq.getReturnUrl());
        context.setCancelUrl(createOrderReq.getCancelUrl());

        // Payment Source
        Paypal paypal = new Paypal();
        paypal.setExperienceContext(context);

        PaymentSource paymentSource = new PaymentSource();
        paymentSource.setPaypal(paypal);

        // Main Object
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setIntent(Constant.INTENT_CAPTURE);
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
 		headers.add(Constant.PAYPAL_REQUEST_ID, uuid);
 		
 		// Prepare HttpRequest
 		HttpRequest httpRequest = new HttpRequest();
 		httpRequest.setHttpMethod(HttpMethod.POST);
 		
		httpRequest.setUrl(createOrderUrl);
 		httpRequest.setHeaders(headers);
 		httpRequest.setBody(reqJson);
        
 		// TODO Make HTTP Call using HttpServiceEngine
 		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP Response from HttpServiceEngine: {}", response);
 		
		log.info("Order created successfully in PaymentServiceImpl");
		
		return response.getBody();
	}
}