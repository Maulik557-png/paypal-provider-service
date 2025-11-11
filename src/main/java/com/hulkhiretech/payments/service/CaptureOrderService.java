package com.hulkhiretech.payments.service;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalShowOrder;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptureOrderService {
	
	private final HttpServiceEngine httpServiceEngine;
	
	private final JsonUtil jsonUtil;
	
	private static final String NO_BODY = "";
	
	public String showOrder(String orderId, String accessToken) {
		log.info("Showing order in CaptureOrderService||orderId: {}, accessToken: {}", orderId, accessToken);
		
		// Prepare headers
 		HttpHeaders headers = new HttpHeaders();
 		headers.setBearerAuth(accessToken);		
 		headers.setContentType(MediaType.APPLICATION_JSON);
 		
 		// Prepare HttpRequest
 		HttpRequest httpRequest = new HttpRequest();
 		httpRequest.setHttpMethod(HttpMethod.GET);
		httpRequest.setUrl("https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderId);
 		httpRequest.setHeaders(headers);
 		httpRequest.setBody(NO_BODY);		// no body for capture order request
 		log.info("HttpRequest prepared for show order: {}", httpRequest);
 		
 		ResponseEntity<String> showOrderResponse = httpServiceEngine.makeHttpCall(httpRequest);
 		log.info("Show order response received: {}", showOrderResponse);
		
 		PaypalShowOrder paypalShowOrder = jsonUtil.fromJson(showOrderResponse.getBody(), PaypalShowOrder.class);
 		log.info("PaypalShowOrder object parsed from response: {}", paypalShowOrder);
 		
 		if(!"APPROVED".equalsIgnoreCase(paypalShowOrder.getStatus())) {
			log.warn("Order is not in APPROVED status, cannot capture order. Current status: {}", paypalShowOrder.getStatus());
			
			// pass formatted json message to processing service
			String paymentPending = jsonUtil.toJson(paypalShowOrder);
			log.info("Warning message to be forwarded: {}", paymentPending);
			
			// TODO forward this message to processing service
			return paymentPending;
		}
 		
 		log.info("Show order completed in CaptureOrderService");
		return paypalShowOrder.getStatus();
	}
	
	public String captureOrder(String orderId, String accessToken) {
		log.info("Capturing order in CaptureOrderService||orderId: {}, accessToken: {}", orderId, accessToken);
		
		// Prepare headers
 		HttpHeaders headers = new HttpHeaders();
 		headers.setBearerAuth(accessToken);		
 		headers.setContentType(MediaType.APPLICATION_JSON);

 		String uuid = UUID.randomUUID().toString();
 		headers.add(Constant.PAYPAL_REQUEST_ID, uuid);
 		
 		// Prepare HttpRequest
 		HttpRequest httpRequest = new HttpRequest();
 		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl("https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/capture");
 		httpRequest.setHeaders(headers);
 		httpRequest.setBody(NO_BODY);		// no body for capture order request
		
		// TODO Make HTTP call to capture order and handle response
 		ResponseEntity<String> captureResponse = httpServiceEngine.makeHttpCall(httpRequest);
 		log.info("Capture order response received: {}", captureResponse);
		
		// convert captureResponse to java object and return relevant info
		
		return captureResponse.getBody();

	}
	
}
