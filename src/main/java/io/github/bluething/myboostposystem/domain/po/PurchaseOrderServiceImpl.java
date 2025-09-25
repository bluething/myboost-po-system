package io.github.bluething.myboostposystem.domain.po;

import io.github.bluething.myboostposystem.common.TimezoneUtil;
import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import io.github.bluething.myboostposystem.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderHeaderRepository purchaseOrderHeaderRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<POData> findAll(Pageable pageable) {
        log.debug("Finding all purchase orders with pageable: {}", pageable);

        Page<PurchaseOrderHeader> purchaseOrders = purchaseOrderHeaderRepository.findAll(pageable);
        return purchaseOrders.map(this::toData);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<POData> findById(Integer id) {
        log.debug("Finding purchase order with id: {}", id);

        return purchaseOrderHeaderRepository.findByIdWithDetails(id).map(this::toData);
    }

    @Override
    public POData create(CreatePOCommand createDto) {
        log.info("Creating new purchase order");

        // Validate items exist
        List<Integer> itemIds = createDto.details().stream()
                .map(CreatePODetail::itemId)
                .collect(Collectors.toList());

        Map<Integer, Item> itemsMap = itemRepository.findAllById(itemIds)
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        validateItemsExist(itemIds, itemsMap);

        PurchaseOrderHeader purchaseOrder = buildPurchaseOrderHeader(createDto, itemsMap);
        PurchaseOrderHeader savedPurchaseOrder = purchaseOrderHeaderRepository.save(purchaseOrder);

        log.info("Purchase order created with id: {}", savedPurchaseOrder.getId());
        return toData(savedPurchaseOrder);
    }

    @Override
    public POData update(Integer id, UpdatePOCommand dto) {
        log.info("Updating purchase order with id: {}", id);

        PurchaseOrderHeader existingPurchaseOrder = purchaseOrderHeaderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + id));

        updatePurchaseOrderFields(existingPurchaseOrder, dto);

        if (dto.details() != null && !dto.details().isEmpty()) {
            List<Integer> itemIds = dto.details().stream()
                    .map(CreatePODetail::itemId)
                    .collect(Collectors.toList());

            Map<Integer, Item> itemsMap = itemRepository.findAllById(itemIds)
                    .stream()
                    .collect(Collectors.toMap(Item::getId, Function.identity()));

            validateItemsExist(itemIds, itemsMap);
            updatePurchaseOrderDetails(existingPurchaseOrder, dto, itemsMap);
        }

        PurchaseOrderHeader savedPurchaseOrder = purchaseOrderHeaderRepository.save(existingPurchaseOrder);
        log.info("Purchase order updated with id: {}", id);

        return toData(savedPurchaseOrder);
    }

    @Override
    public boolean deleteById(Integer id) {
        log.info("Deleting purchase order with id: {}", id);

        if (!purchaseOrderHeaderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Purchase Order not found with id: " + id);
        }

        purchaseOrderHeaderRepository.deleteById(id);
        log.info("Purchase order deleted with id: {}", id);
        return true;
    }

    private void validateItemsExist(List<Integer> itemIds, Map<Integer, Item> itemsMap) {
        List<Integer> missingItemIds = itemIds.stream()
                .filter(id -> !itemsMap.containsKey(id))
                .toList();

        if (!missingItemIds.isEmpty()) {
            throw new ResourceNotFoundException("Items not found with ids: " + missingItemIds);
        }
    }

    private void updatePurchaseOrderFields(PurchaseOrderHeader existingPurchaseOrder, UpdatePOCommand dto) {
        if (dto.datetime() != null) {
            existingPurchaseOrder.setDatetime(TimezoneUtil.fromAppZone(dto.datetime()));
        }
        if (dto.description() != null) {
            existingPurchaseOrder.setDescription(dto.description());
        }
        existingPurchaseOrder.setUpdatedBy(dto.updatedBy());
    }

    private PurchaseOrderHeader buildPurchaseOrderHeader(CreatePOCommand dto, Map<Integer, Item> itemsMap) {
        List<PurchaseOrderDetail> details = dto.details().stream()
                .map(detail -> buildPurchaseOrderDetail(detail, itemsMap))
                .toList();

        long totalCost = calculateTotalCost(details);
        long totalPrice = calculateTotalPrice(details);

        PurchaseOrderHeader purchaseOrder = PurchaseOrderHeader.builder()
                .datetime(TimezoneUtil.fromAppZone(dto.datetime()))
                .description(dto.description())
                .totalCost(totalCost)
                .totalPrice(totalPrice)
                .details(details)
                .build();
        Instant now = Instant.now();
        purchaseOrder.setCreatedBy(dto.createdBy());
        purchaseOrder.setUpdatedBy(dto.createdBy());
        purchaseOrder.setCreatedDatetime(now);
        purchaseOrder.setUpdatedDatetime(now);

        // Set back reference
        details.forEach(detail -> detail.setPurchaseOrderHeader(purchaseOrder));

        return purchaseOrder;
    }
    private long calculateTotalCost(List<PurchaseOrderDetail> details) {
        return details.stream()
                .mapToLong(PurchaseOrderDetail::getTotalCost)
                .sum();
    }

    private long calculateTotalPrice(List<PurchaseOrderDetail> details) {
        return details.stream()
                .mapToLong(PurchaseOrderDetail::getTotalPrice)
                .sum();
    }
    private PurchaseOrderDetail buildPurchaseOrderDetail(CreatePODetail detail, Map<Integer, Item> itemsMap) {
        Item item = itemsMap.get(detail.itemId());

        return PurchaseOrderDetail.builder()
                .item(item)
                .itemQty(detail.quantity())
                .itemCost(detail.cost() != null ? detail.unitPrice() : item.getCost())
                .itemPrice(detail.unitPrice() != null ? detail.unitPrice() : item.getPrice())
                .build();
    }

    private void updatePurchaseOrderDetails(PurchaseOrderHeader purchaseOrder, UpdatePOCommand dto, Map<Integer, Item> itemsMap) {
        // Clear existing details
        purchaseOrder.getDetails().clear();

        // Add new details
        List<PurchaseOrderDetail> newDetails = dto.details().stream()
                .map(detailRequest -> buildPurchaseOrderDetail(detailRequest, itemsMap))
                .collect(Collectors.toList());

        newDetails.forEach(detail -> detail.setPurchaseOrderHeader(purchaseOrder));
        purchaseOrder.getDetails().addAll(newDetails);

        // Recalculate totals
        purchaseOrder.setTotalCost(calculateTotalCost(newDetails));
        purchaseOrder.setTotalPrice(calculateTotalPrice(newDetails));
    }

    POData toData(PurchaseOrderHeader entity) {
        if (entity == null) {
            return null;
        }

        return new POData(
                entity.getId(),
                TimezoneUtil.toAppLocalDateTime(entity.getDatetime()),
                entity.getDescription(),
                entity.getTotalPrice(),
                entity.getTotalCost(),
                mapToDetailData(entity.getDetails()),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedDatetime(),
                entity.getUpdatedDatetime()
        );
    }
    List<CreatePODetail> mapToDetailData(List<PurchaseOrderDetail> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(entity -> new CreatePODetail(
                        entity.getId(),
                        entity.getItemQty(),
                        entity.getItemPrice(),
                        entity.getItemCost()
                ))
                .toList();
    }
}
