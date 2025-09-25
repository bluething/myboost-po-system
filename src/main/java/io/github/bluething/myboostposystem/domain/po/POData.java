package io.github.bluething.myboostposystem.domain.po;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record POData(Integer id,
                     LocalDateTime orderDate,
                     String description,
                     Long totalPrice,
                     Long totalCost,
                     List<CreatePODetail> details,
                     String createdBy,
                     String updatedBy,
                     Instant createdDatetime,
                     Instant updatedDatetime) {
}
