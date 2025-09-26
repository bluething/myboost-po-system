package io.github.bluething.myboostposystem.domain.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ItemService {
    /**
     * Create a new item
     * @param itemDto the item to create
     * @return the created item
     */
    ItemData create(CreateItemCommand itemDto);

    /**
     * Get all items with pagination
     * @param pageable pagination parameters
     * @return page of items
     */
    Page<ItemData> findAll(Pageable pageable);

    /**
     * Get item by id
     * @param id the item id
     * @return the item if found
     */
    Optional<ItemData> findById(Integer id);

    /**
     * Update an existing item
     * @param id the item id
     * @param itemDto the updated item data
     * @return the updated item if found
     */
    Optional<ItemData> update(Integer id, UpdateItemCommand itemDto);

    /**
     * Delete an item by id
     * @param id the item id
     * @return true if deleted, false if not found
     */
    boolean delete(Integer id);
}
