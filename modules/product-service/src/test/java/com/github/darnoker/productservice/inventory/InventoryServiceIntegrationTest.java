package com.github.darnoker.productservice.inventory;

import com.github.darnoker.productservice.inventory.model.ReservationResult;
import com.github.darnoker.productservice.inventory.model.AdjustStockCommand;
import com.github.darnoker.productservice.inventory.model.ConfirmReservationsCommand;
import com.github.darnoker.productservice.inventory.model.ExpireReservationsCommand;
import com.github.darnoker.productservice.inventory.model.ReleaseReservationsCommand;
import com.github.darnoker.productservice.inventory.model.ReserveStockCommand;
import com.github.darnoker.productservice.inventory.model.ReservedItem;
import com.github.darnoker.productservice.inventory.persistence.InventoryEntity;
import com.github.darnoker.productservice.inventory.persistence.InventoryRepository;
import com.github.darnoker.productservice.inventory.persistence.StockReservationEntity;
import com.github.darnoker.productservice.inventory.persistence.StockReservationRepository;
import com.github.darnoker.productservice.product.ProductType;
import com.github.darnoker.productservice.product.persistence.ProductEntity;
import com.github.darnoker.productservice.product.persistence.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryServiceIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockReservationRepository stockReservationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void reservesStockAndReturnsExistingReservationForRepeatedRequest() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        Instant now = Instant.parse("2026-08-23T12:00:00Z");
        productRepository.save(new ProductEntity(
                productId,
                "Test book",
                "Product used by the inventory test",
                new BigDecimal("10.00"),
                ProductType.BOOK,
                "{}",
                now,
                now));
        inventoryRepository.save(new InventoryEntity(productId, 10, 0, now));

        ReserveStockCommand command = new ReserveStockCommand(
                orderId,
                requestId,
                List.of(new ReservedItem(productId, 3)));

        List<ReservationResult> firstResult = inventoryService.reserveStock(command);
        List<ReservationResult> repeatedResult = inventoryService.reserveStock(command);

        assertEquals(1, firstResult.size());
        assertEquals(firstResult, repeatedResult);
        assertNotNull(firstResult.getFirst().reservationId());

        InventoryEntity inventory = inventoryRepository.findById(productId).orElseThrow();
        assertEquals(10, inventory.getQuantity());
        assertEquals(3, inventory.getReservedQuantity());

        List<StockReservationEntity> reservations = stockReservationRepository.findAllByOrderId(orderId);
        assertEquals(1, reservations.size());
        StockReservationEntity reservation = reservations.getFirst();
        assertEquals(productId, reservation.getProductId());
        assertEquals(requestId, reservation.getRequestId());
        assertEquals(3, reservation.getQuantity());
        assertEquals(StockReservationStatus.RESERVED, reservation.getStatus());
        assertEquals(firstResult.getFirst().reservationId(), reservation.getId());
    }

    @Test
    void confirmsReservationByDeductingPhysicalAndReservedStock() {
        UUID productId = createProductWithInventory(10, 3);
        UUID orderId = UUID.randomUUID();
        StockReservationEntity reservation = createReservation(productId, orderId, 3, Instant.now().plusSeconds(900));

        inventoryService.confirmReservations(new ConfirmReservationsCommand(orderId, UUID.randomUUID()));

        InventoryEntity inventory = inventoryRepository.findById(productId).orElseThrow();
        assertEquals(7, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(StockReservationStatus.CONFIRMED, stockReservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
    }

    @Test
    void releasesAndExpiresReservationsByReturningReservedStock() {
        UUID productId = createProductWithInventory(10, 5);
        UUID releasedOrderId = UUID.randomUUID();
        StockReservationEntity released = createReservation(productId, releasedOrderId, 2, Instant.now().plusSeconds(900));
        StockReservationEntity expired = createReservation(productId, UUID.randomUUID(), 3, Instant.now().minusSeconds(1));

        inventoryService.releaseReservations(new ReleaseReservationsCommand(releasedOrderId, UUID.randomUUID(), "cancelled"));
        inventoryService.expireReservations(new ExpireReservationsCommand());

        InventoryEntity inventory = inventoryRepository.findById(productId).orElseThrow();
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(StockReservationStatus.RELEASED, stockReservationRepository.findById(released.getId()).orElseThrow().getStatus());
        assertEquals(StockReservationStatus.EXPIRED, stockReservationRepository.findById(expired.getId()).orElseThrow().getStatus());
    }

    @Test
    void preventsStockAdjustmentBelowReservedQuantity() {
        UUID productId = createProductWithInventory(10, 3);

        inventoryService.adjustStock(new AdjustStockCommand(productId, -7, "count correction", UUID.randomUUID()));
        assertEquals(3, inventoryRepository.findById(productId).orElseThrow().getQuantity());

        assertThrows(StockAdjustmentBelowReservedQuantityException.class, () -> inventoryService.adjustStock(
                new AdjustStockCommand(productId, -1, "count correction", UUID.randomUUID())));
    }

    @Test
    void rejectsZeroStockAdjustment() {
        UUID productId = createProductWithInventory(10, 0);

        assertThrows(InvalidStockAdjustmentException.class, () -> inventoryService.adjustStock(
                new AdjustStockCommand(productId, 0, "count correction", UUID.randomUUID())));
    }

    private UUID createProductWithInventory(int quantity, int reservedQuantity) {
        UUID productId = UUID.randomUUID();
        Instant now = Instant.now();
        productRepository.save(new ProductEntity(productId, "Test product", "Product used by the inventory test",
                new BigDecimal("10.00"), ProductType.BOOK, "{}", now, now));
        inventoryRepository.save(new InventoryEntity(productId, quantity, reservedQuantity, now));
        return productId;
    }

    private StockReservationEntity createReservation(UUID productId, UUID orderId, int quantity, Instant expiresAt) {
        StockReservationEntity reservation = new StockReservationEntity(UUID.randomUUID(), productId, orderId, UUID.randomUUID(),
                quantity, StockReservationStatus.RESERVED, expiresAt, Instant.now());
        return stockReservationRepository.save(reservation);
    }
}
