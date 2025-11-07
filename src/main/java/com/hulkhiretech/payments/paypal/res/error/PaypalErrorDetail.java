package com.hulkhiretech.payments.paypal.res.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaypalErrorDetail {

    private String field;
    private String value;
    private String location;
    private String issue;
    private String description;
}