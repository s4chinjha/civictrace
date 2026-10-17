package com.civictrace.issue;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public final class IssueRequests {

    private IssueRequests() {
    }

    public record Create(
            @NotBlank(message = "Title is required")
            @Size(max = 160, message = "Title must be at most 160 characters")
            String title,
            @NotBlank(message = "Description is required")
            @Size(max = 5000, message = "Description must be at most 5000 characters")
            String description,
            @NotNull(message = "Category is required") IssueCategory category,
            @NotNull(message = "Latitude is required")
            @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal latitude,
            @NotNull(message = "Longitude is required")
            @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal longitude,
            @Size(max = 240, message = "Location must be at most 240 characters") String locationLabel
    ) {
    }

    public record StatusChange(
            @NotNull(message = "New status is required") IssueStatus status,
            @Size(max = 1000, message = "Comment must be at most 1000 characters") String comment
    ) {
    }

    public record Progress(
            @NotBlank(message = "Progress update is required")
            @Size(max = 1000, message = "Progress update must be at most 1000 characters") String comment
    ) {
    }

    public record Assignment(@NotNull(message = "Staff member is required") UUID staffId) {
    }

    public record Suggestions(
            @NotNull(message = "Category is required") IssueCategory category,
            @NotNull(message = "Latitude is required") @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
            @NotNull(message = "Longitude is required") @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
    ) {
    }
}
