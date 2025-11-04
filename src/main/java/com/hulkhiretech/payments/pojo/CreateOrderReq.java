package com.hulkhiretech.payments.pojo;

import lombok.Data;

@Data
public class CreateOrderReq {
	
	private String currencyCode;
	
	private double amount;
	
	private String cancelUrl;
	
	private String returnUrl;
}
