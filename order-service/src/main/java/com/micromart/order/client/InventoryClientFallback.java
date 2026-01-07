package com.micromart.order.client;

import com.micromart.order.client.dto.StockReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for Inventory Client.
 * <p>
 * When inventory service is unavailable, this provides degraded behavior.
 */
@Component
@Slf4j
public class InventoryClientFallback implements InventoryClient {

    @Override
    public Boolean checkStock(Long productId, int quantity) {
        log.warn("Inventory service unavailable - fallback for checkStock({}, {})", productId, quantity);
        // Fail safe: assume no stock when service unavailable
        return false;
    }

    @Override
    public Object reserveStock(StockReservationDto request) {
        log.warn("Inventory service unavailable - fallback for reserveStock({})", request);
        // Return null to indicate failure
        return null;
    }

    @Override
    public Object releaseStock(StockReservationDto request) {
        log.warn("Inventory service unavailable - fallback for releaseStock({})", request);
        return null;
    }

    @Override
    public Object confirmReservation(StockReservationDto request) {
        log.warn("Inventory service unavailable - fallback for confirmReservation({})", request);
        return null;
    }
}
