package io.github.bluething.myboostposystem.domain.po;

public record CreatePODetail(Integer itemId,
                             Integer quantity,
                             Long unitPrice,
                             Long cost) {
}
