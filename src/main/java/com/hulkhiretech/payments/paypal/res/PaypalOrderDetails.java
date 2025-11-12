package com.hulkhiretech.payments.paypal.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaypalOrderDetails {

	@JsonProperty("id")
    private String orderId;
    private String intent;
    @JsonProperty("status")
    private String paypalStatus;
    
    @JsonProperty("create_time")
    private String createTime;
}
