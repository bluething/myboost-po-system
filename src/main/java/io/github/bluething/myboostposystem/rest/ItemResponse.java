package io.github.bluething.myboostposystem.rest;

import java.time.LocalDateTime;

record ItemResponse(Integer id,
                           String name,
                           String description,
                           Integer price,
                           Integer cost,
                           String createdBy,
                           String updatedBy,
                           LocalDateTime createdDatetime,
                           LocalDateTime updatedDatetime) {
}
