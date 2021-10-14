package com.ecommerce.order.controller;

import com.ecommerce.order.repository.dbo.OrderDBO;
import com.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1")
public class OrderController {

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/status/{statusUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDBO getOrderStatus(Authentication authentication, @PathVariable UUID statusUuid) throws AccessDeniedException {
        return this.orderService.getOrderStatus(authentication, statusUuid);
    }

    @GetMapping(value = "/buyer/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderDBO> getBuyerOrderHistory(Authentication authentication) {
        return this.orderService.getBuyerOrderHistory(authentication);
    }
}
