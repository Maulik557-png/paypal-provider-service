package com.hulkhiretech.payments.paypal.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaypalShowOrder {

    private String id;
    private String intent;
    private String status;
    @JsonProperty("create_time")
    private String createTime;
}
