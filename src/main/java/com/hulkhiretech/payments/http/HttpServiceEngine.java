package com.hulkhiretech.payments.http;

import java.util.function.Consumer;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpServiceEngine {

	private final RestClient restClient;

	public String makeHttpCall()	{	

		log.info("Making Http call in HttpServiceEngine");
		
		HttpHeaders headers = new HttpHeaders();
		String clientID = "AbisXxOv9XcvjxT-6BAguN24o5QkBLLcvFV0YpIYlWUJCZgUfWxJsREdVN_kxSEmCuOAA-BVoMcHkPxz"; 
		String clientSecret = "EJEiW9g-vuQ28yr1KBDa6_1cP48zvSIe_KCPSiUJNnxTcXTr8pJmtpJxpvRxya9z6UxvODevw2GbjGva"; 
		headers.setBasicAuth(clientID, clientSecret);
		headers.set("Content-Type", "application/x-www-form-urlencoded");
		
		class ConsumerHeaderObj implements Consumer<HttpHeaders>    {

			HttpHeaders applicationHeader;

			public ConsumerHeaderObj(HttpHeaders applicationHeader) {
				this.applicationHeader = applicationHeader;
			}

			@Override
			public void accept(HttpHeaders restClientHeader) {
				restClientHeader.addAll(this.applicationHeader);
			}
		}
		
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();  
		formData.add("grant_type", "client_credentials");

		try {

			String httpResponse = restClient.method(HttpMethod.POST)
					.uri("https://api-m.sandbox.paypal.com/v1/oauth2/token")
					.headers(new ConsumerHeaderObj(headers))
					.body(formData)
					.retrieve()
					.body(String.class);

			log.info("HTTP call completed httpResponse  ");
			
			return httpResponse;
		}
		catch (Exception e) {
			log.error(clientSecret);
			throw new RuntimeException("HTTP call failed in HttpServiceEngine" + ": " + e.getMessage());} 
	}
}
