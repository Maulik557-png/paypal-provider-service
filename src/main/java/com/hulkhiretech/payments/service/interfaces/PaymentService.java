package com.hulkhiretech.payments.service.interfaces;

import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;

public interface PaymentService {
	public OrderResponse createOrder(CreateOrderReq createOrderReq);
	
	public String captureOrder(String orderId);
}