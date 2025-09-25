package io.github.bluething.myboostposystem.domain.po;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreatePOCommand(LocalDateTime datetime,
                              String description,
                              Long totalPrice,
                              Long totalCost,
                              String createdBy,
                              List<CreatePODetail> details) {
}
