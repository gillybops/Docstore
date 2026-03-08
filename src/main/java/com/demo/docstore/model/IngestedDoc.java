package com.demo.docstore.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * IngestedDoc — the core domain model representing a document stored in
 * AWS DocumentDB (MongoDB-compatible).
 *
 * Annotations explained:
 *
 *   @Document(collection = "documents")
 *     → Spring Data MongoDB annotation. Maps this class to the "documents"
 *       collection in DocumentDB. Equivalent to a table in SQL.
 *
 *   @Data         (Lombok) → generates getters, setters, equals, hashCode, toString
 *   @NoArgsConstructor    → generates a no-arg constructor (required by Jackson for deserialization)
 *   @AllArgsConstructor   → generates a constructor with all fields
 *
 *   @Schema (SpringDoc) → used to populate Swagger UI with field descriptions
 *     and example values. This is what makes the auto-generated docs useful.
 */
@Document(collection = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A document ingested into the DocStore system")
public class IngestedDoc {

    /**
     * @Id → marks this field as the MongoDB document _id.
     * DocumentDB auto-generates a 24-character hex ObjectId string
     * (e.g. "65f1a2b3c4d5e6f7a8b9c0d1") when a document is saved.
     */
    @Id
    @Schema(description = "Auto-generated MongoDB ObjectId", example = "65f1a2b3c4d5e6f7a8b9c0d1", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    /**
     * @NotBlank → Spring Validation: rejects null, empty, or whitespace-only strings.
     * @Size     → enforces max length before hitting the database.
     * Validation is triggered by @Valid on the controller method parameter.
     */
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Document title", example = "Q3 Architecture Notes")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 100000, message = "Content must not exceed 100,000 characters")
    @Schema(description = "Full document content", example = "This document covers the proposed microservice topology...")
    private String content;

    /**
     * ingestedAt is set server-side in the controller — clients never send this field.
     * Using LocalDateTime (not Date) because it's timezone-agnostic and serializes
     * cleanly to ISO-8601 format in JSON via Jackson's JavaTimeModule.
     */
    @Schema(description = "Server-side timestamp when the document was ingested",
            example = "2024-03-15T14:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime ingestedAt;

    /**
     * Optional metadata tags — stored as a simple string array in DocumentDB.
     * Demonstrates MongoDB's flexible schema: SQL would require a join table.
     */
    @Schema(description = "Optional tags for categorization", example = "[\"architecture\", \"aws\", \"q3\"]")
    private String[] tags;
}
