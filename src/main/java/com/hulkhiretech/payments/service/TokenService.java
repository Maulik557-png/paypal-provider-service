package com.hulkhiretech.payments.service;

import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.http.HttpServiceEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

	private final HttpServiceEngine httpServiceEngine; 


	// TODO Implement Redis and take care of expiry
	private static String accessToken;

	public String getAccessToken()	{

		log.info("Retriving Access Token from TokenService");

		if(accessToken != null)	{
			log.info("Returning cached Access Token");
			return accessToken;
		}

		log.info("No cached Access Token found, Calling OAuth service");

		String response = httpServiceEngine.makeHttpCall();

		log.info("HTTP Response from HttpServiceEngine: {}", response);
		
		return response;
	}

}