package guru.sfg.beer.order.service.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message msg) {
        boolean isValid = true;

        ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

        if (request.getBeerOrder().getCustomerRef() != null) {
            if (("fail-validation").equals(request.getBeerOrder().getCustomerRef())) { //condition to fail validation
                isValid = false;
            } else if (("cancel-from-validation-pending").equals(request.getBeerOrder().getCustomerRef())) { //don't send message if cancelled
                return;
            }
        }

        System.out.println("############### VALIDATION LISTENER RAN ###############");

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                ValidateOrderResult.builder()
                        .isValid(isValid)
                        .orderId(request.getBeerOrder().getId())
                        .build());
    }
}
