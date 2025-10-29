package com.hulkhiretech.payments.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import lombok.Data;

@Data
public class HttpRequest {

	HttpMethod httpMethod;
	String url;
	HttpHeaders headers;
	Object body;

}
