package com.demo.docstore.controller;

import com.demo.docstore.model.IngestedDoc;
import com.demo.docstore.repository.DocRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DocController — handles all HTTP requests for the /api/docs endpoints.
 *
 * Key annotations:
 *
 *   @RestController
 *     → Combines @Controller + @ResponseBody. Every method return value
 *       is automatically serialized to JSON via Jackson. No need to call
 *       ObjectMapper or response.write() manually.
 *
 *   @RequestMapping("/api/docs")
 *     → Base path prefix applied to all methods in this class.
 *
 *   @CrossOrigin — handled globally in CorsConfig.java instead.
 *
 *   @Tag (SpringDoc) → groups endpoints under a named section in Swagger UI.
 */
@RestController
@RequestMapping("/api/docs")
@Tag(name = "Documents", description = "Ingest, retrieve, search, and delete documents stored in AWS DocumentDB")
public class DocController {

    /**
     * @Autowired → Spring injects the DocRepository bean here at startup.
     * The repository implementation is generated at runtime by Spring Data —
     * there is no DocRepositoryImpl class to write.
     */
    @Autowired
    private DocRepository docRepository;

    // ── POST /api/docs ────────────────────────────────────────────────────────

    /**
     * Ingest a new document into DocumentDB.
     *
     * @Valid triggers Spring Validation on the request body.
     * If @NotBlank or @Size constraints fail, Spring returns a 400 Bad Request
     * automatically — no try/catch needed for validation errors.
     *
     * @RequestBody → deserializes the incoming JSON payload into an IngestedDoc
     * using Jackson. Field names in JSON must match field names in the class.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // returns 201 instead of 200 on success
    @Operation(
        summary = "Ingest a document",
        description = "Stores a new document in AWS DocumentDB. The id and ingestedAt fields are generated server-side."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Document ingested successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed — title or content missing/too long")
    })
    public IngestedDoc ingest(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Document to ingest. Do not include id or ingestedAt — these are set by the server.",
                required = true
            )
            IngestedDoc doc) {

        // Set the server-side ingestion timestamp before saving
        doc.setIngestedAt(LocalDateTime.now());

        // save() performs an upsert: INSERT if id is null, UPDATE if id exists.
        // DocumentDB auto-generates the ObjectId and populates doc.getId().
        return docRepository.save(doc);
    }

    // ── GET /api/docs ─────────────────────────────────────────────────────────

    /**
     * Retrieve all documents, with optional title or content search.
     *
     * @RequestParam(required = false) → query parameter is optional.
     * /api/docs           → returns all documents
     * /api/docs?title=aws → returns docs with "aws" in the title
     * /api/docs?content=microservice → returns docs with "microservice" in content
     */
    @GetMapping
    @Operation(
        summary = "List all documents",
        description = "Returns all ingested documents. Use ?title= or ?content= to filter."
    )
    public List<IngestedDoc> getAll(
            @Parameter(description = "Filter by title keyword (case-insensitive)")
            @RequestParam(required = false) String title,

            @Parameter(description = "Filter by content keyword (case-insensitive)")
            @RequestParam(required = false) String content) {

        if (title != null && !title.isBlank()) {
            return docRepository.findByTitleContainingIgnoreCase(title);
        }
        if (content != null && !content.isBlank()) {
            return docRepository.searchByContent(content);
        }
        return docRepository.findAll();
    }

    // ── GET /api/docs/{id} ────────────────────────────────────────────────────

    /**
     * Retrieve a single document by its MongoDB ObjectId.
     *
     * @PathVariable → binds the {id} URL segment to the method parameter.
     *
     * ResponseEntity<IngestedDoc> → gives us control over the HTTP status code.
     * If the document exists: 200 OK + document body.
     * If not found: 404 Not Found + error message.
     *
     * orElseThrow() → throws ResponseStatusException which Spring maps to a
     * proper HTTP error response automatically.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a document by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document found"),
        @ApiResponse(responseCode = "404", description = "No document with that ID",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<IngestedDoc> getById(
            @Parameter(description = "MongoDB ObjectId of the document", example = "65f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String id) {

        return docRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Document not found with id: " + id));
    }

    // ── GET /api/docs/tag/{tag} ───────────────────────────────────────────────

    /**
     * Filter documents by tag — demonstrates MongoDB array field querying.
     * This would require a JOIN table in a relational database.
     * In DocumentDB, tags are stored as an array inside the document itself.
     */
    @GetMapping("/tag/{tag}")
    @Operation(
        summary = "Get documents by tag",
        description = "Returns all documents that contain the specified tag in their tags array."
    )
    public List<IngestedDoc> getByTag(
            @Parameter(description = "Tag to filter by", example = "architecture")
            @PathVariable String tag) {
        return docRepository.findByTagsContaining(tag);
    }

    // ── DELETE /api/docs/{id} ─────────────────────────────────────────────────

    /**
     * Delete a document by ID.
     *
     * @ResponseStatus(NO_CONTENT) → returns 204 with no body on success.
     * This is the HTTP convention for successful DELETE operations.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a document by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Document deleted"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public void delete(
            @Parameter(description = "MongoDB ObjectId to delete")
            @PathVariable String id) {

        if (!docRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Document not found with id: " + id);
        }
        docRepository.deleteById(id);
    }

    // ── GET /api/docs/health ──────────────────────────────────────────────────

    /**
     * Simple health check endpoint — useful for AWS Elastic Beanstalk health
     * monitoring and load balancer target group health checks.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns 200 OK if the API is running")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "service", "docstore-api",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
