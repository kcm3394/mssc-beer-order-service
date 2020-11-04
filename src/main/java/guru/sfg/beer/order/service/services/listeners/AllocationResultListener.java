package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult event) {

        if (!event.getAllocationError() && !event.getPendingInventory()) {
            //allocated normally
            beerOrderManager.beerOrderAllocationPassed(event.getBeerOrderDto());
        } else if (!event.getAllocationError() && event.getPendingInventory()){
            //pending inventory
            beerOrderManager.beerOrderAllocationPendingInventory(event.getBeerOrderDto());
        } else {
            //allocation error
            beerOrderManager.beerOrderAllocationFailed(event.getBeerOrderDto());
        }
    }
}
