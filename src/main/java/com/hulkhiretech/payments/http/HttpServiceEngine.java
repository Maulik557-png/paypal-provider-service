package com.hulkhiretech.payments.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpServiceEngine is responsible for making HTTP calls using RestClient.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HttpServiceEngine {

	private final RestClient restClient;
	
	/**
	 * Makes an HTTP call based on the provided HttpRequest.
	 * 
	 * @param httpRequest the HttpRequest containing request details
	 * @return the ResponseEntity containing the response
	 * @throws PaypalProviderException if an error occurs during the HTTP call
	 */
	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest)	{	
		log.debug("Preparing to make Http call in HttpServiceEngine||httpRequest: {}", httpRequest);
		
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
		} catch(HttpClientErrorException | HttpServerErrorException  e) {
			// Valid error response from server 4xx/5xx
			log.error("HTTP error response received: StatusCode: {}, ResponseBody: {}",
					e.getStatusCode(), e.getResponseBodyAsString());
			
			// if the error is gateway timeout or service unavailable, throw service PaypalProviderException
			if(e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT || e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
				log.error("Paypal service is unavailable or gateway timeout");
				throw new PaypalProviderException(
						ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorCode(),
						ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorMessage(),
						HttpStatus.SERVICE_UNAVAILABLE);
			}
			
			// return response entity to invoker
			return ResponseEntity
					.status(e.getStatusCode())
					.body(e.getResponseBodyAsString());
		} catch (Exception e) {
			// No response from server / other exceptions
			log.error("Exception while preparing request: {}", e.getMessage());
			throw new PaypalProviderException(
					ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorCode(),
					ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorMessage(),
					HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
}
