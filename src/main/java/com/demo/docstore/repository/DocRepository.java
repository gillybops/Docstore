package com.demo.docstore.repository;

import com.demo.docstore.model.IngestedDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DocRepository — the data access layer for IngestedDoc documents.
 *
 * Extends MongoRepository<IngestedDoc, String> where:
 *   - IngestedDoc → the domain object this repository manages
 *   - String      → the type of the @Id field (MongoDB ObjectId as a String)
 *
 * MongoRepository provides out-of-the-box CRUD methods at no cost:
 *   save(), findById(), findAll(), deleteById(), count(), existsById(), etc.
 *
 * Spring Data generates the implementation at runtime via reflection —
 * there is no SQL or boilerplate to write. The method names below follow
 * Spring Data's "derived query" naming convention, which the framework
 * parses and converts into MongoDB filter queries automatically.
 *
 * Example:  findByTitleContainingIgnoreCase("aws")
 *   → db.documents.find({ title: { $regex: "aws", $options: "i" } })
 */
@Repository
public interface DocRepository extends MongoRepository<IngestedDoc, String> {

    /**
     * Case-insensitive title search.
     * Spring Data parses "ContainingIgnoreCase" and generates the regex query.
     *
     * Equivalent MongoDB query:
     *   db.documents.find({ title: { $regex: keyword, $options: "i" } })
     */
    List<IngestedDoc> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Filter documents ingested after a specific timestamp.
     * Useful for "show me everything ingested today" queries.
     *
     * Equivalent MongoDB query:
     *   db.documents.find({ ingestedAt: { $gt: ISODate("...") } })
     */
    List<IngestedDoc> findByIngestedAtAfter(LocalDateTime since);

    /**
     * @Query annotation — when derived method names get complex, write the
     * MongoDB JSON query directly. This searches the content field with a
     * case-insensitive regex, equivalent to a full-text LIKE in SQL.
     *
     * ?0 = first method parameter (keyword)
     */
    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    List<IngestedDoc> searchByContent(String keyword);

    /**
     * Count documents per tag — demonstrates MongoDB array field querying.
     * The $in operator checks if the tag exists in the tags array field.
     */
    List<IngestedDoc> findByTagsContaining(String tag);
}
