package com.ecommerce.services;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ecommerce.domain.Order;

@FeignClient(name = "api-gateway-service")
public interface GatewayClient {
	
	@RequestMapping(method=RequestMethod.POST, value="/api/order/bulk")
	public boolean createBulkOrder(@RequestBody List<Order> orders);
}