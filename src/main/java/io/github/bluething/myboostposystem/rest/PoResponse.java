package io.github.bluething.myboostposystem.rest;

import java.time.LocalDate;
import java.util.List;

record PoResponse(Integer id,
                  String poNumber,
                  Integer supplierId,
                  LocalDate orderDate,
                  String status,
                  String remarks,
                  Long totalAmount,                // sum of lineAmount (rupiah)
                  List<PoDetailResponse> details) {
}
