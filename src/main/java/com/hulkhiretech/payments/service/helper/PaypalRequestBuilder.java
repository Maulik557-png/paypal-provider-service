package com.hulkhiretech.payments.service.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.req.Amount;
import com.hulkhiretech.payments.paypal.req.ExperienceContext;
import com.hulkhiretech.payments.paypal.req.OrderRequest;
import com.hulkhiretech.payments.paypal.req.PaymentSource;
import com.hulkhiretech.payments.paypal.req.Paypal;
import com.hulkhiretech.payments.paypal.req.PurchaseUnit;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Builder class to prepare HttpRequest objects for PayPal API calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaypalRequestBuilder {

	private final JsonUtil jsonUtil;
	
	@Value("${paypal.create.order.url}")
	private String createOrderUrl;
	
	@Value("${paypal.client.id}")
	private String clientID;

	@Value("${paypal.client.secret}")
	private String clientSecret;

	@Value("${paypal.outh.url}")
	private String oauthUrl;
	
	@Value("${paypal.capture.order.url}")
	private String captureOrderUrlTemplate;
	
	@Value("${paypal.show.order.url}")
	private String showOrderUrlTemplate;
	
	/**
	 * Prepares the HttpRequest for obtaining an OAuth token from PayPal.
	 * 
	 * @return the prepared HttpRequest
	 */
	public HttpRequest prepareTokenRequest() {
		
		// Prepare headers and body
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(clientID, clientSecret);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultiValueMap<String, String> reqBody = new LinkedMultiValueMap<>();  
		reqBody.add(Constant.GRANT_TYPE, Constant.CLIENT_CREDENTIALS);
		
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(oauthUrl);
		httpRequest.setHeaders(headers);
		httpRequest.setBody(reqBody);
		return httpRequest;
	}
	
	/**
	 * Prepares the HttpRequest for creating an order with PayPal.
	 * 
	 * @param createOrderReq the CreateOrderReq object containing order details
	 * @param accessToken    the access token for authentication
	 * @return the prepared HttpRequest
	 */
	public HttpRequest prepareCreateOrderRequest(CreateOrderReq createOrderReq, String accessToken) {
		// Amount
        Amount amount = new Amount();
        amount.setCurrencyCode(createOrderReq.getCurrencyCode());
        amount.setValue(String.format(Constant.TWO_DECIMAL_FORMAT, createOrderReq.getAmount()));

        // Purchase Unit
        PurchaseUnit purchaseUnit = new PurchaseUnit();
        String timestamp = DateTimeFormatter.ofPattern(Constant.YYYY_MM_DD_HHMMSS).format(LocalDateTime.now());
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        String invoiceId = Constant.INVOICE_TEMPLATE + timestamp + "-" + randomNum;
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
        
        String reqJson = jsonUtil.toJson(orderRequest);
		log.info("Create Order Request JSON: {}", reqJson);

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
		return httpRequest;
	}
	
	public HttpRequest prepareShowOrderRequest(String orderId, String accessToken) {
		log.info("Preparing HttpRequest for show order||orderId: {}", orderId);
		
		// Prepare headers
  		HttpHeaders headers = new HttpHeaders();
  		headers.setBearerAuth(accessToken);		
  		headers.setContentType(MediaType.APPLICATION_JSON);

  		// Prepare HttpRequest
  		HttpRequest httpRequest = new HttpRequest();
  		httpRequest.setHttpMethod(HttpMethod.GET);
		String showOrderUrl = showOrderUrlTemplate.replace(Constant.ORDER_ID_REF, orderId);
		httpRequest.setUrl(showOrderUrl);
  		httpRequest.setHeaders(headers);
  		httpRequest.setBody(Constant.NO_BODY);		// no body for show order request
  		
  		log.info("HttpRequest prepared for show order: {}", httpRequest);
		return httpRequest;
	}
	
	public HttpRequest prepareCaptureOrderRequest(String orderId, String accessToken) {
		log.info("Preparing HttpRequest for capture order||orderId: {}", orderId);
		
		// Prepare headers
  		HttpHeaders headers = new HttpHeaders();
  		headers.setBearerAuth(accessToken);		
  		headers.setContentType(MediaType.APPLICATION_JSON);

  		String uuid = UUID.randomUUID().toString();
  		log.info("Generated UUID for PayPal-Request-Id header: {}", uuid);
  		headers.add(Constant.PAYPAL_REQUEST_ID, uuid);
  		
  		// Prepare HttpRequest
  		HttpRequest httpRequest = new HttpRequest();
  		httpRequest.setHttpMethod(HttpMethod.POST);
		String captureOrderUrl = captureOrderUrlTemplate.replace(Constant.ORDER_ID_REF, orderId);
		httpRequest.setUrl(captureOrderUrl);
  		httpRequest.setHeaders(headers);
  		httpRequest.setBody(Constant.NO_BODY);		// no body for capture order request
  		
  		log.info("HttpRequest prepared for capture order: {}", httpRequest);
		return httpRequest;
	}
}
