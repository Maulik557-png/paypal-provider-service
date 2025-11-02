package com.hulkhiretech.payments.service.interfaces;

import com.hulkhiretech.payments.pojo.CreateOrderReq;

public interface PaymentService {
	public String createOrder(CreateOrderReq createOrderReq);
}