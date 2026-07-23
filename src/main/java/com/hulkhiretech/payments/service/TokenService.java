package com.hulkhiretech.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalOAuthToken;
import com.hulkhiretech.payments.service.helper.PaypalRequestBuilder;
import com.hulkhiretech.payments.service.helper.PaypalResponseMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing PayPal access tokens.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

	private final RedisService redisService;
	
	private final PaymentValidator paymentValidator;
	
	private final HttpServiceEngine httpServiceEngine; 
	
	private final PaypalRequestBuilder paypalRequestBuilder;
	
	private final PaypalResponseMapper paypalResponseMapper;
	
	/**
	 * Retrieves the access token, either from cache or by making an OAuth request.
	 * 
	 * @return the access token as a String
	 */
	public String getAccessToken()	{
		log.info("Retriving Access Token from TokenService");
		
		String accessToken = redisService.getValue(Constant.PAYPAL_ACCESS_TOKEN);
		log.info("Access Token from Redis cache: {}", accessToken);
		
		if(accessToken != null)	{
			log.info("Returning cached Access Token from Redis");
			return accessToken;
		}

		log.info("No cached Access Token found, Calling OAuth service");

		HttpRequest httpRequest = paypalRequestBuilder.prepareTokenRequest();	

		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP Response from HttpServiceEngine: {}", response);
		
		PaypalOAuthToken token = paypalResponseMapper.prepareTokenResponse(response);
		log.info("PaypalOAuthToken mapped from response: {}", token);
		
		paymentValidator.validateAccessToken(token.getAccessToken());
		accessToken = token.getAccessToken();
		log.info("Access Token retrieved: {}", accessToken);
		
		redisService.setValueWithExpiry(
				Constant.PAYPAL_ACCESS_TOKEN, 
				accessToken, 
				token.getExpiresIn() - Constant.REDIS_TOKEN_EXPIRY_BUFFER_TIME);	// Subtracting 300 seconds to avoid expiry during use
		log.info("Access Token cached in Redis with expiry");
		
		return accessToken;
	}
}
