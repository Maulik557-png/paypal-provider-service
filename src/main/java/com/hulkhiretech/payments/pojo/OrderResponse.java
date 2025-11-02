package com.hulkhiretech.payments.pojo;

import lombok.Data;

@Data
public class OrderResponse {
	
	String orderId;
	
	String status;
	
	String redirectUrl;
	
}
