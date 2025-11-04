package com.hulkhiretech.payments.paypal.res;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaypalOrder {
	
	private String id;
	
    private String status;

    private List<PaypalLink> links;
	
}
