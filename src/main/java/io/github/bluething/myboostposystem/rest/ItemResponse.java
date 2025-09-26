package io.github.bluething.myboostposystem.rest;

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
