package com.hulkhiretech.payments.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
	
	private String orderId;
	private String intent;
	private String paypalStatus;
	private String redirectUrl;
	
	@JsonProperty("create_time")
    private String createTime;
	
}
