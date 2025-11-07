package com.hulkhiretech.payments.paypal.res.error;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaypalErrorResponse {

    // Fields for the general PayPal API error
    private String name;
    private String message;

    @JsonProperty("debug_id")
    private String debugId;

    private List<PaypalErrorDetail> details;
    private List<PaypalErrorLink> links;

    // Fields for OAuth-style error
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}