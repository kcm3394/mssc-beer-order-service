package guru.sfg.beer.order.service.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg) {
        boolean isError = false;
        boolean isPartial = false;

        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();

        if (request.getBeerOrder().getCustomerRef() != null) {
            if (("fail-allocation").equals(request.getBeerOrder().getCustomerRef())) { //condition to fail allocation
                isError = true;
            } else if (("partial-allocation").equals(request.getBeerOrder().getCustomerRef())) { //condition for partial allocation
                isPartial = true;
            } else if (("cancel-from-allocation-pending").equals(request.getBeerOrder().getCustomerRef())) { //don't send message if cancelled
                return;
            }
        }

        boolean finalIsPartial = isPartial;
        request.getBeerOrder().getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (finalIsPartial) {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
            } else {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
            }
        });

        System.out.println("############### ALLOCATION LISTENER RAN ###############");

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder()
                        .beerOrderDto(request.getBeerOrder())
                        .allocationError(isError)
                        .pendingInventory(isPartial)
                        .build());
    }
}
