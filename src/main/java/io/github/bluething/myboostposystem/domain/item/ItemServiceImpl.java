package io.github.bluething.myboostposystem.domain.item;

import io.github.bluething.myboostposystem.exception.ResourceNotFoundException;
import io.github.bluething.myboostposystem.persistence.Item;
import io.github.bluething.myboostposystem.persistence.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public ItemData create(CreateItemCommand itemDto) {
        log.debug("Creating new item: {}", itemDto);

        Item entity = toEntity(itemDto);
        Item savedEntity = itemRepository.save(entity);

        log.debug("Item created with id: {}", savedEntity.getId());
        return toData(savedEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ItemData> findAll(Pageable pageable) {
        log.debug("Finding all items with pageable: {}", pageable);

        Page<Item> items = itemRepository.findAll(pageable);
        return items.map(this::toData);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ItemData> findById(Integer id) {
        log.debug("Finding item with id: {}", id);

        return itemRepository.findById(id).map(this::toData);
    }

    @Override
    public Optional<ItemData> update(Integer id, UpdateItemCommand itemDto) {
        log.debug("Updating item with id: {}, data: {}", id, itemDto);

        if (id == null || itemDto == null) {
            return Optional.empty();
        }

        return itemRepository.findById(id)
                .map(existingItem -> {
                    Item savedItem = toEntity(existingItem, itemDto);
                    Item updatedEntity = itemRepository.save(savedItem);
                    log.debug("Item updated with id: {}", updatedEntity.getId());
                    return toData(updatedEntity);
                });
    }

    @Override
    public boolean delete(Integer id) {
        log.debug("Deleting item with id: {}", id);

        if (id == null) {
            return false;
        }

        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }

        itemRepository.deleteById(id);
        log.debug("Item deleted with id: {}", id);
        return true;
    }

    private ItemData toData(Item item) {
        if (item == null) {
            return null;
        }

        return new ItemData(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCost(),
                item.getCreatedBy(),
                item.getUpdatedBy(),
                item.getCreatedDatetime(),
                item.getUpdatedDatetime());
    }

    private Item toEntity(CreateItemCommand itemDto) {
        if (itemDto == null) {
            throw new IllegalArgumentException("ItemDto cannot be null");
        }

        Instant now = Instant.now();
        Item item = Item.builder()
                .name(itemDto.name())
                .description(itemDto.description())
                .price(itemDto.price())
                .cost(itemDto.cost()).build();
        item.setCreatedBy(itemDto.createdBy());
        item.setUpdatedBy(itemDto.createdBy());
        item.setCreatedDatetime(now);
        item.setUpdatedDatetime(now);

        return item;
    }

    private Item toEntity(Item existingItem, UpdateItemCommand itemDto) {
        if (existingItem == null || itemDto == null) {
            return null;
        }

        Item item = existingItem.toBuilder()
                .name(itemDto.name())
                .description(itemDto.description())
                .price(itemDto.price())
                .cost(itemDto.cost()).build();
        item.setCreatedBy(existingItem.getCreatedBy());
        item.setUpdatedBy(itemDto.updatedBy());
        item.setCreatedDatetime(existingItem.getCreatedDatetime());
        item.setUpdatedDatetime(Instant.now());

        return item;
    }
}
