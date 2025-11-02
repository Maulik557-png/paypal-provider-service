package com.hulkhiretech.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalOAuthToken;
import com.hulkhiretech.payments.service.helper.PaypalRequestBuilder;
import com.hulkhiretech.payments.service.helper.PaypalResponseMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

	private final HttpServiceEngine httpServiceEngine; 
	
	private final PaypalRequestBuilder paypalRequestBuilder;
	
	private final PaypalResponseMapper paypalResponseMapper;

	// TODO Implement Redis and take care of expiry
	private static String accessToken;
	
	/**
	 * Method to get Access Token from OAuth service
	 * @return access token as String
	 */
	public String getAccessToken()	{

		log.info("Retriving Access Token from TokenService");

		if(accessToken != null)	{
			log.info("Returning cached Access Token");
			return accessToken;
		}

		log.info("No cached Access Token found, Calling OAuth service");

		HttpRequest httpRequest = paypalRequestBuilder.prepareTokenRequest();	

		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP Response from HttpServiceEngine: {}", response);
		
		PaypalOAuthToken token = paypalResponseMapper.prepareTokenResponse(response);
		
		accessToken = token.getAccessToken();
		log.info("Access Token retrived accessToken: {}", accessToken);
		return accessToken;
	}
}
