package io.github.bluething.myboostposystem.rest.item;

import java.time.LocalDateTime;

record ItemResponse(Integer id,
                    String name,
                    String description,
                    Long price,
                    Long cost,
                    String createdBy,
                    String updatedBy,
                    LocalDateTime createdDatetime,
                    LocalDateTime updatedDatetime) {
}
