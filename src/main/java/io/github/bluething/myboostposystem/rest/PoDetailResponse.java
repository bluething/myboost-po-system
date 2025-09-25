package io.github.bluething.myboostposystem.rest;

record PoDetailResponse(Integer id,
                               Integer lineNo,
                               Integer itemId,
                               Integer quantity,
                               Long unitPrice,
                               Long lineAmount,
                               String note) {
}
