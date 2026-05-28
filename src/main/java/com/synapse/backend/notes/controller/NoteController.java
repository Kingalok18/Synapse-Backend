package com.synapse.backend.notes.controller;

import com.synapse.backend.dto.ApiResponse;
import com.synapse.backend.notes.dto.CanvasPositionDTO;
import com.synapse.backend.notes.dto.NoteRequest;
import com.synapse.backend.notes.dto.NoteResponse;
import com.synapse.backend.notes.service.NoteService;
import com.synapse.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Endpoints for managing workspace notes and visual synapse canvas data")
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @Operation(summary = "Create a new note", description = "Creates a note with title, content, custom tags, coordinates, and linkages.")
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @Valid @RequestBody NoteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NoteResponse response = noteService.createNote(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Note created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a note", description = "Updates title, content, tags, and connections of an existing note.")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @PathVariable String id,
            @Valid @RequestBody NoteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NoteResponse response = noteService.updateNote(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a note", description = "Sets deleted flag to true on the note.")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        noteService.deleteNote(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Note deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get note by ID", description = "Retrieves details of a note by its document ID.")
    public ResponseEntity<ApiResponse<NoteResponse>> getNoteById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NoteResponse response = noteService.getNoteById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Note retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "List notes with pagination, sorting and filtering", description = "Retrieves filtered list of user notes.")
    public ResponseEntity<ApiResponse<Page<NoteResponse>>> getAllNotes(
            @RequestParam(required = false) Boolean isPinned,
            @RequestParam(required = false) Boolean isArchived,
            @RequestParam(required = false) String tag,
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Page<NoteResponse> response = noteService.getAllNotes(userPrincipal.getId(), isPinned, isArchived, tag, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notes listed successfully", response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search notes", description = "Search title, content, and tags case-insensitively.")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> searchNotes(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<NoteResponse> response = noteService.searchNotes(query, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", response));
    }

    @PatchMapping("/pin/{id}")
    @Operation(summary = "Toggle or set pinned status", description = "Sets pinned status or toggles current status if pinned query parameter is omitted.")
    public ResponseEntity<ApiResponse<NoteResponse>> pinNote(
            @PathVariable String id,
            @RequestParam(required = false) Boolean pinned,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NoteResponse response = noteService.setPinned(id, pinned, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Note pin status updated successfully", response));
    }

    @PatchMapping("/archive/{id}")
    @Operation(summary = "Toggle or set archived status", description = "Sets archived status or toggles current status if archived query parameter is omitted.")
    public ResponseEntity<ApiResponse<NoteResponse>> archiveNote(
            @PathVariable String id,
            @RequestParam(required = false) Boolean archived,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NoteResponse response = noteService.setArchived(id, archived, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Note archived status updated successfully", response));
    }

    @PatchMapping("/canvas/{id}")
    @Operation(summary = "Update canvas coordinates", description = "Updates x and y coordinates offset on the visual network canvas.")
    public ResponseEntity<ApiResponse<NoteResponse>> updateCanvas(
            @PathVariable String id,
            @Valid @RequestBody CanvasPositionDTO position,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NoteResponse response = noteService.updateCanvasPosition(id, position, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Canvas position updated successfully", response));
    }

    @PatchMapping("/connections/{id}")
    @Operation(summary = "Update notes connections linkages", description = "Updates linked note connection list mappings.")
    public ResponseEntity<ApiResponse<NoteResponse>> updateConnections(
            @PathVariable String id,
            @RequestBody List<String> connectionIds,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NoteResponse response = noteService.updateConnections(id, connectionIds, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Connections updated successfully", response));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recently updated notes", description = "Retrieves the top 10 most recently updated notes.")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getRecent(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<NoteResponse> response = noteService.getRecentNotes(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Recent notes retrieved successfully", response));
    }
}
