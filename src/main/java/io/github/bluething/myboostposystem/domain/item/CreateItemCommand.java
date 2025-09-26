package io.github.bluething.myboostposystem.domain.item;

public record CreateItemCommand(String name,
                                String description,
                                Long price,
                                Long cost,
                                String createdBy) {
}
