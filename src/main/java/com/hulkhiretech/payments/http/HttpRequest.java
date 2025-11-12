package com.hulkhiretech.payments.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import lombok.Data;

/**
 * Represents an HTTP request with method, URL, headers, and body.
 */
@Data
public class HttpRequest {

	HttpMethod httpMethod;
	String url;
	HttpHeaders headers;
	Object body;

}
