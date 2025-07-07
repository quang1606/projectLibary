package com.example.demospringbath.processor;

import com.example.demospringbath.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class OrderProcessor implements ItemProcessor<Order, Order> {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);

    @Override
    public Order process(Order order) throws Exception {
        // Validate quantity > 10
        if (order.getQuantity() > 10) {
            log.info("Skipping order with id {}: quantity ({}) > 10", order.getId(), order.getQuantity());
            return null; // Trả về null để loại bỏ item này
        }

        // Validate price < 100
        if (order.getPrice() < 100) {
            log.warn("Skipping order with id {}: price ({}) < 100", order.getId(), order.getPrice());
            return null; // Trả về null để loại bỏ item này
        }

        log.info("Processing valid order: {}", order);
        return order; // Trả về item hợp lệ để ghi vào DB
    }
}