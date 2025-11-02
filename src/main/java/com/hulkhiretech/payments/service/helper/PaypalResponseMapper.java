package com.hulkhiretech.payments.service.helper;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.hulkhiretech.payments.paypal.res.PaypalLink;
import com.hulkhiretech.payments.paypal.res.PaypalOAuthToken;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaypalResponseMapper {
	private final JsonUtil jsonUtil;

	public PaypalOAuthToken prepareTokenResponse(ResponseEntity<String> response) {
		return jsonUtil.fromJson(response.getBody(), PaypalOAuthToken.class);
	}
	
	public OrderResponse prepareOrderResponse(ResponseEntity<String> response)	{

		PaypalOrder paypalOrder = jsonUtil.fromJson(response.getBody(), PaypalOrder.class);
		log.info("PaypalOrder object created: {}", paypalOrder);

		OrderResponse orderResponse = new OrderResponse();
		orderResponse.setOrderId(paypalOrder.getId());
		orderResponse.setStatus(paypalOrder.getStatus());

		String redirectUrl = paypalOrder.getLinks().stream()
				.filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
				.map(PaypalLink::getHref)
				.findFirst()
				.orElse(null);

		orderResponse.setRedirectUrl(redirectUrl);

		return orderResponse;
	}
}
