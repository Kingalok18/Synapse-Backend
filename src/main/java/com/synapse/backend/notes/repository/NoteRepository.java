package com.synapse.backend.notes.repository;

import com.synapse.backend.notes.document.NoteDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends MongoRepository<NoteDocument, String> {

    Optional<NoteDocument> findByIdAndUserIdAndDeletedFalse(String id, UUID userId);

    Page<NoteDocument> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    List<NoteDocument> findByUserIdAndDeletedFalse(UUID userId, Sort sort);

    // Recent notes: last modified non-deleted notes
    List<NoteDocument> findTop10ByUserIdAndDeletedFalseOrderByUpdatedAtDesc(UUID userId);

    // Search query on title, content, or tags case-insensitively
    @Query("{ 'userId': ?0, 'deleted': false, '$or': [ { 'title': { '$regex': ?1, '$options': 'i' } }, { 'content': { '$regex': ?1, '$options': 'i' } }, { 'tags': { '$regex': ?1, '$options': 'i' } } ] }")
    List<NoteDocument> searchNotes(UUID userId, String query);
}
