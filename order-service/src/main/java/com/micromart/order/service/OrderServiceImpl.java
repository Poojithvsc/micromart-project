package com.micromart.order.service;

import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.order.client.InventoryClient;
import com.micromart.order.client.ProductClient;
import com.micromart.order.client.dto.ProductDto;
import com.micromart.order.client.dto.StockReservationDto;
import com.micromart.order.domain.Order;
import com.micromart.order.domain.OrderItem;
import com.micromart.order.domain.OrderStatus;
import com.micromart.order.dto.request.CreateOrderRequest;
import com.micromart.order.dto.request.OrderItemRequest;
import com.micromart.order.kafka.OrderEventProducer;
import com.micromart.order.mapper.OrderMapper;
import com.micromart.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Order Service Implementation.
 * <p>
 * PEAA Patterns:
 * - Service Layer: Business logic encapsulation
 * - Unit of Work: @Transactional manages transactions
 * - Gateway: Feign clients for inter-service communication
 * - Domain Events: Kafka events for order lifecycle
 * <p>
 * Learning Points:
 * - Coordinating multiple services (Product, Inventory)
 * - Handling distributed transaction scenarios
 * - Circuit breaker integration via Feign fallbacks
 * - Event-driven architecture with Kafka
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for user {}", request.getUserId());

        // 1. Validate products and check stock
        List<ProductDto> products = validateAndGetProducts(request.getItems());
        checkStockAvailability(request.getItems());

        // 2. Create order
        Order order = Order.builder()
                .userId(request.getUserId())
                .shippingAddress(orderMapper.toAddress(request.getShippingAddress()))
                .notes(request.getNotes())
                .currency("USD")
                .items(new ArrayList<>())
                .build();

        // 3. Add items to order
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemRequest = request.getItems().get(i);
            ProductDto product = products.get(i);

            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPriceAmount())
                    .build();

            order.addItem(orderItem);
        }

        // Save order first to get order number
        Order savedOrder = orderRepository.save(order);

        // 4. Reserve inventory for each item
        try {
            reserveInventory(savedOrder);
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order {}", savedOrder.getOrderNumber(), e);
            // In a real system, you might want to use Saga pattern here
            throw new RuntimeException("Failed to reserve inventory: " + e.getMessage(), e);
        }

        log.info("Created order {} with {} items, total: {}",
                savedOrder.getOrderNumber(),
                savedOrder.getItems().size(),
                savedOrder.getTotalAmount());

        // 5. Publish order created event to Kafka
        orderEventProducer.publishOrderCreated(savedOrder);

        return savedOrder;
    }

    @Override
    public Order getById(Long id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order getByOrderNumber(String orderNumber) {
        return orderRepository.findWithItemsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> findByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public List<Order> findByUserIdAndStatus(Long userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional
    public Order confirmOrder(Long orderId) {
        log.info("Confirming order {}", orderId);

        Order order = getById(orderId);
        order.confirm();

        Order savedOrder = orderRepository.save(order);

        // Publish order confirmed event
        orderEventProducer.publishOrderConfirmed(savedOrder);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId) {
        log.info("Cancelling order {}", orderId);

        Order order = getById(orderId);

        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Release reserved inventory
        releaseInventory(order);

        order.cancel();

        Order savedOrder = orderRepository.save(order);

        // Publish order cancelled event (for other services)
        orderEventProducer.publishOrderCancelled(savedOrder);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order shipOrder(Long orderId) {
        log.info("Shipping order {}", orderId);

        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.PAYMENT_COMPLETED) {
            throw new IllegalStateException("Order must have payment completed before shipping");
        }

        // Confirm inventory reservation (deduct actual stock)
        confirmInventory(order);

        order.markShipped();

        Order savedOrder = orderRepository.save(order);

        // Publish order shipped event
        orderEventProducer.publishOrderShipped(savedOrder);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order deliverOrder(Long orderId) {
        log.info("Delivering order {}", orderId);

        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order must be shipped before delivery");
        }

        order.markDelivered();

        Order savedOrder = orderRepository.save(order);

        // Publish order delivered event
        orderEventProducer.publishOrderDelivered(savedOrder);

        return savedOrder;
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public long countByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Validate products exist and get their details.
     */
    private List<ProductDto> validateAndGetProducts(List<OrderItemRequest> items) {
        List<ProductDto> products = new ArrayList<>();

        for (OrderItemRequest item : items) {
            ProductDto product = productClient.getProduct(item.getProductId());

            if (product == null) {
                throw new ResourceNotFoundException("Product", "id", item.getProductId());
            }

            if (!product.isActive()) {
                throw new IllegalStateException("Product " + item.getProductId() + " is not available");
            }

            products.add(product);
        }

        return products;
    }

    /**
     * Check stock availability for all items.
     */
    private void checkStockAvailability(List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            Boolean available = inventoryClient.checkStock(item.getProductId(), item.getQuantity());

            if (available == null || !available) {
                throw new IllegalStateException(
                        "Insufficient stock for product " + item.getProductId() +
                        ". Requested: " + item.getQuantity());
            }
        }
    }

    /**
     * Reserve inventory for order items.
     */
    private void reserveInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            StockReservationDto reservation = StockReservationDto.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .orderReference(order.getOrderNumber())
                    .build();

            Object result = inventoryClient.reserveStock(reservation);

            if (result == null) {
                throw new RuntimeException("Failed to reserve stock for product " + item.getProductId());
            }
        }

        log.info("Reserved inventory for order {}", order.getOrderNumber());
    }

    /**
     * Release reserved inventory (on cancellation).
     */
    private void releaseInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                StockReservationDto reservation = StockReservationDto.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .orderReference(order.getOrderNumber())
                        .build();

                inventoryClient.releaseStock(reservation);
            } catch (Exception e) {
                log.error("Failed to release inventory for product {} in order {}",
                        item.getProductId(), order.getOrderNumber(), e);
                // Continue releasing other items
            }
        }

        log.info("Released inventory for cancelled order {}", order.getOrderNumber());
    }

    /**
     * Confirm inventory reservation (deduct actual stock on shipment).
     */
    private void confirmInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            StockReservationDto reservation = StockReservationDto.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .orderReference(order.getOrderNumber())
                    .build();

            Object result = inventoryClient.confirmReservation(reservation);

            if (result == null) {
                log.warn("Failed to confirm inventory for product {} in order {}",
                        item.getProductId(), order.getOrderNumber());
            }
        }

        log.info("Confirmed inventory for shipped order {}", order.getOrderNumber());
    }
}
