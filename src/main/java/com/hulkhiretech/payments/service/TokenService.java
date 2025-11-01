package com.hulkhiretech.payments.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalOAuthToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

	private final HttpServiceEngine httpServiceEngine; 
	private final ObjectMapper objectMapper;

	@Value("${paypal.client.id}")
	private String clientID;

	@Value("${paypal.client.secret}")
	private String clientSecret;

	@Value("${paypal.outh.url}")
	private String oauthUrl;

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

		HttpHeaders headers = new HttpHeaders();

		headers.setBasicAuth(clientID, clientSecret);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();  
		formData.add(Constant.GRANT_TYPE, Constant.CLIENT_CREDENTIALS);
		
		HttpRequest httpRequest = new HttpRequest();

		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(oauthUrl);
		httpRequest.setHeaders(headers);
		httpRequest.setBody(formData);	

		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP Response from HttpServiceEngine: {}", response);
		
		try {
			PaypalOAuthToken token = objectMapper.readValue(response.getBody(), PaypalOAuthToken.class);
			accessToken = token.getAccessToken();
			log.info("Access Token retrived accessToken: {}", accessToken);
			
			return "Access token: " + token.getAccessToken() + "\nExpires in: " + token.getExpiresIn();
		} catch (Exception e) {
			log.error("Exception while parsing OAuth response: {}", e.getMessage());
			throw new RuntimeException("Failed to parse OAuth response: " + e.getMessage());
		}	
	}
}
