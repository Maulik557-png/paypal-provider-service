package com.hulkhiretech.payments.paypal.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperienceContext {

    @JsonProperty("payment_method_preference")
    private String paymentMethodPreference;

    @JsonProperty("landing_page")
    private String landingPage;

    @JsonProperty("shipping_preference")
    private String shippingPreference;

    @JsonProperty("user_action")
    private String userAction;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;
}
