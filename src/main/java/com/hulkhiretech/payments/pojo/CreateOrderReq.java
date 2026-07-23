package com.hulkhiretech.payments.pojo;

import lombok.Data;

/* 
 * NOTE: DO NOT CHANGE VARIABLE ORDER AS IT MAY AFFECT SERIALIZATION
 * VARIABLE NAMES ARE ALPHABETICALLY SORTED
 */
@Data
public class CreateOrderReq {
	
	private Double amount;
	
	private String cancelUrl;
	
	private String currencyCode;
	
	private String returnUrl;
}
