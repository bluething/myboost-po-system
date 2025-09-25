package io.github.bluething.myboostposystem.rest.po;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

record Response(Integer id,
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                        LocalDateTime datetime,
                String description,
                Long totalPrice,
                Long totalCost,
                String createdBy,
                String updatedBy,
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                        LocalDateTime createdDatetime,
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                        LocalDateTime updatedDatetime,
                List<DetailResponse> details) {
}
