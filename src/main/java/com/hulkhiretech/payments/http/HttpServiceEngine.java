package com.hulkhiretech.payments.http;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpServiceEngine {

	private final RestClient restClient;
	

	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest)	{	
		
		log.info("Making Http call in HttpServiceEngine");
		
		try {

			ResponseEntity<String> httpResponse = restClient
					.method(httpRequest.getHttpMethod())
					.uri(httpRequest.getUrl())
					.headers(
							restClientHeader -> 
							restClientHeader.addAll(
									httpRequest.getHeaders()))		// lambda
					.body(httpRequest.getBody())
					.retrieve()
					.toEntity(String.class);

			log.info("HTTP call completed httpResponse: {}", httpResponse);	
			return httpResponse;
		}
		catch (Exception e) {
			log.error("Exception while preparing request: {}", e.getMessage());
			throw new RuntimeException("HTTP call failed in HttpServiceEngine" + ": " + e.getMessage());
		}
	}
}
