package io.github.bluething.myboostposystem.rest;

import java.util.List;

public record PagedResponse<T>(List<T> content,
                               PageMetadata page) {
}
