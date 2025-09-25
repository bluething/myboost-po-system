package io.github.bluething.myboostposystem.rest;

public record PageMetadata(int number,
                           int size,
                           long totalElements,
                           int totalPages,
                           boolean first,
                           boolean last,
                           boolean hasNext,
                           boolean hasPrevious) {
}
