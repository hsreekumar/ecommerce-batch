package com.ecommerce.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.domain.Order;
import com.ecommerce.services.kafka.KafkaBatchProducer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@RestController
@CrossOrigin
@RequestMapping("/batch")
@Api(value = "Order Api")
public class OrderController {

	@Autowired
	KafkaBatchProducer orderService;


	/**
	 * Create batch order
	 * Submits to kafka topic to be processed in bulk
	 * @param Order
	 * @return
	 * @throws Exception 
	 */
	@ApiOperation(value = "Create order")
	@PostMapping("/order")
	public void createOrder(@RequestBody Order order) throws Exception {
		orderService.submit(order);
	}
	
}