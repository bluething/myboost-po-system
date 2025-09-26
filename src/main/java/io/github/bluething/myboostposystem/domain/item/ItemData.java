package io.github.bluething.myboostposystem.domain.item;

import java.time.Instant;

public record ItemData(Integer id,
                       String name,
                       String description,
                       Long price,
                       Long cost,
                       String createdBy,
                       String updatedBy,
                       Instant createdDatetime,
                       Instant updatedDatetime) {
}
