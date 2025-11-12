package com.hulkhiretech.payments.service.impl;

import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.CaptureOrderService;
import com.hulkhiretech.payments.service.CreateOrderService;
import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.helper.PaypalResponseMapper;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final TokenService tokenService;

	private final CreateOrderService createOrderService;

	private final CaptureOrderService captureOrderService;

	private final PaypalResponseMapper paypalResponseMapper;

	/**
	 * Creates an order using the provided CreateOrderReq.
	 * 
	 * @param createOrderReq the CreateOrderReq object containing order details
	 * @return the OrderResponse containing order details
	 */
	@Override
	public OrderResponse createOrder(CreateOrderReq createOrderReq) {
		log.debug("Creating order in PaymentServiceImpl||createOrderReq: {}", createOrderReq);

		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived: {}", accessToken);

		OrderResponse orderResponse = createOrderService.createOrder(createOrderReq, accessToken);
		log.info("OrderResponse received from createOrder method: {}", orderResponse);

		// TODO TimeOut handling using Circuit Breaker pattern 

		log.info("Order created successfully in PaymentServiceImpl");
		return orderResponse;
	}

	/**
	 * Captures the order with the given orderId.
	 * 
	 * @param orderId the ID of the order to be captured
	 * @return the OrderResponse containing captured order details
	 */
	@Override
	public OrderResponse captureOrder(String orderId) {
		log.info("Capturing order in PaymentServiceImpl||orderId: {}", orderId);

		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrived for capture order: {}", accessToken);

		PaypalOrder orderStatus = captureOrderService.showOrder(orderId, accessToken);
		log.info("Order details retrieved using showOrder: {}", orderStatus);

		if (!Constant.APPROVED.equalsIgnoreCase(orderStatus.getStatus())) {
			log.warn("Order status is not APPROVED. Current status: {}", orderStatus);

			OrderResponse paymentPendingResponse = paypalResponseMapper.pendingPaymentResponse(orderStatus);
			log.info("Order is not in APPROVED status. Returning pending payment response: {}", paymentPendingResponse);

			return paymentPendingResponse;
		}

		OrderResponse captureResponse = captureOrderService.captureOrder(orderId, accessToken);
		log.info("OrderResponse received from captureOrder method: {}", captureResponse);

		log.info("Order captured successfully in PaymentServiceImpl");
		return captureResponse;
	}

}