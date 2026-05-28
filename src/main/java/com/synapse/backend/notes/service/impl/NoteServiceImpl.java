package com.synapse.backend.notes.service.impl;

import com.synapse.backend.exception.BusinessException;
import com.synapse.backend.exception.ResourceNotFoundException;
import com.synapse.backend.notes.document.NoteDocument;
import com.synapse.backend.notes.dto.CanvasPositionDTO;
import com.synapse.backend.notes.dto.NoteRequest;
import com.synapse.backend.notes.dto.NoteResponse;
import com.synapse.backend.notes.mapper.NoteMapper;
import com.synapse.backend.notes.repository.NoteRepository;
import com.synapse.backend.notes.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public NoteResponse createNote(NoteRequest request, UUID userId) {
        NoteDocument note = noteMapper.toDocument(request);
        note.setUserId(userId);
        note.setDeleted(false);

        // Ensure canvasPosition is initialized if null
        if (note.getCanvasPosition() == null) {
            note.setCanvasPosition(new NoteDocument.CanvasPosition(0.0, 0.0));
        }

        // Validate that specified connection IDs exist and belong to this user
        if (note.getConnections() != null && !note.getConnections().isEmpty()) {
            for (String connId : note.getConnections()) {
                noteRepository.findByIdAndUserIdAndDeletedFalse(connId, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Connection note not found: " + connId));
            }
        } else {
            note.setConnections(new java.util.ArrayList<>());
        }

        if (note.getTags() == null) {
            note.setTags(new java.util.ArrayList<>());
        }

        NoteDocument saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NoteResponse updateNote(String id, NoteRequest request, UUID userId) {
        NoteDocument note = noteRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or you are not the owner"));

        noteMapper.updateDocumentFromRequest(request, note);

        // Validate connections
        if (note.getConnections() != null && !note.getConnections().isEmpty()) {
            for (String connId : note.getConnections()) {
                if (connId.equals(id)) {
                    throw new BusinessException("A note cannot connect to itself", HttpStatus.BAD_REQUEST);
                }
                noteRepository.findByIdAndUserIdAndDeletedFalse(connId, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Connection note not found: " + connId));
            }
        }

        NoteDocument saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteNote(String id, UUID userId) {
        NoteDocument note = noteRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or you are not the owner"));

        // Soft delete
        note.setDeleted(true);
        noteRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(String id, UUID userId) {
        NoteDocument note = noteRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or you are not the owner"));
        return noteMapper.toResponse(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> getAllNotes(UUID userId, Boolean isPinned, Boolean isArchived, String tag, Pageable pageable) {
        Query query = new Query().with(pageable);
        query.addCriteria(Criteria.where("userId").is(userId).and("deleted").is(false));

        if (isPinned != null) {
            query.addCriteria(Criteria.where("isPinned").is(isPinned));
        }

        if (isArchived != null) {
            query.addCriteria(Criteria.where("isArchived").is(isArchived));
        }

        if (tag != null && !tag.trim().isEmpty()) {
            query.addCriteria(Criteria.where("tags").is(tag)); // checks if 'tag' is present in tags array
        }

        List<NoteDocument> notes = mongoTemplate.find(query, NoteDocument.class);

        Page<NoteDocument> page = PageableExecutionUtils.getPage(
                notes,
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), NoteDocument.class)
        );

        return page.map(noteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String query, UUID userId) {
        if (query == null || query.trim().isEmpty()) {
            return noteRepository.findByUserIdAndDeletedFalse(userId, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "updatedAt"))
                    .stream()
                    .map(noteMapper::toResponse)
                    .collect(Collectors.toList());
        }
        return noteRepository.searchNotes(userId, query)
                .stream()
                .map(noteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteResponse setPinned(String id, Boolean pinned, UUID userId) {
        NoteDocument note = noteRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or you are not the owner"));

        if (pinned != null) {
            note.setPinned(pinned);
        } else {
            note.setPinned(!note.isPinned()); // Toggle if null
        }

        NoteDocument saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NoteResponse setArchived(String id, Boolean archived, UUID userId) {
        NoteDocument note = noteRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or you are not the owner"));

        if (archived != null) {
            note.setArchived(archived);
        } else {
            note.setArchived(!note.isArchived()); // Toggle if null
        }

        NoteDocument saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NoteResponse updateCanvasPosition(String id, CanvasPositionDTO position, UUID userId) {
        NoteDocument note = noteRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or you are not the owner"));

        if (position == null) {
            throw new BusinessException("Position coordinates cannot be null", HttpStatus.BAD_REQUEST);
        }

        note.setCanvasPosition(new NoteDocument.CanvasPosition(position.getX(), position.getY()));
        NoteDocument saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NoteResponse updateConnections(String id, List<String> connectionIds, UUID userId) {
        NoteDocument note = noteRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or you are not the owner"));

        if (connectionIds != null && !connectionIds.isEmpty()) {
            for (String connId : connectionIds) {
                if (connId.equals(id)) {
                    throw new BusinessException("A note cannot connect to itself", HttpStatus.BAD_REQUEST);
                }
                noteRepository.findByIdAndUserIdAndDeletedFalse(connId, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Connection note not found: " + connId));
            }
            note.setConnections(connectionIds);
        } else {
            note.setConnections(new java.util.ArrayList<>());
        }

        NoteDocument saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getRecentNotes(UUID userId) {
        return noteRepository.findTop10ByUserIdAndDeletedFalseOrderByUpdatedAtDesc(userId)
                .stream()
                .map(noteMapper::toResponse)
                .collect(Collectors.toList());
    }
}
