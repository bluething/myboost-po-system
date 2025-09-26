package io.github.bluething.myboostposystem.domain.item;

public record UpdateItemCommand(String name,
                                String description,
                                Long price,
                                Long cost,
                                String updatedBy) {
}
