package com.hulkhiretech.payments.paypal.res.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaypalErrorLink {

    private String href;
    private String rel;

    @JsonProperty("encType")
    private String encType;
}
