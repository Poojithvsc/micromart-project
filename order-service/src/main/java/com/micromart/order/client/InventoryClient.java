package com.micromart.order.client;

import com.micromart.order.client.dto.StockReservationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Inventory Service Feign Client.
 * <p>
 * Communicates with product-service inventory endpoints.
 * Uses circuit breaker for fault tolerance.
 */
@FeignClient(
        name = "product-service",
        contextId = "inventoryClient",
        fallback = InventoryClientFallback.class,
        path = "/inventory"
)
public interface InventoryClient {

    @GetMapping("/check")
    Boolean checkStock(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity);

    @PostMapping("/reserve")
    Object reserveStock(@RequestBody StockReservationDto request);

    @PostMapping("/release")
    Object releaseStock(@RequestBody StockReservationDto request);

    @PostMapping("/confirm")
    Object confirmReservation(@RequestBody StockReservationDto request);
}
