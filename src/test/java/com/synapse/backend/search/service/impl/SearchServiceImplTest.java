package com.synapse.backend.search.service.impl;

import com.synapse.backend.notes.document.NoteDocument;
import com.synapse.backend.notes.dto.NoteResponse;
import com.synapse.backend.notes.mapper.NoteMapper;
import com.synapse.backend.tasks.dto.TaskResponse;
import com.synapse.backend.tasks.mapper.TaskMapper;
import com.synapse.backend.tasks.model.Task;
import com.synapse.backend.tasks.model.TaskPriority;
import com.synapse.backend.tasks.model.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private SearchServiceImpl searchService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void testSearchNotes() {
        Pageable pageable = PageRequest.of(0, 10);
        NoteDocument doc = new NoteDocument();
        NoteResponse res = NoteResponse.builder().id("note-1").title("My Note").build();

        when(mongoTemplate.find(any(Query.class), eq(NoteDocument.class))).thenReturn(List.of(doc));
        when(mongoTemplate.count(any(Query.class), eq(NoteDocument.class))).thenReturn(1L);
        when(noteMapper.toResponse(doc)).thenReturn(res);

        Page<NoteResponse> result = searchService.searchNotes(userId, "test query", "title", "content", List.of("tag1"), false, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(res, result.getContent().get(0));

        verify(mongoTemplate).find(any(Query.class), eq(NoteDocument.class));
        verify(mongoTemplate).count(any(Query.class), eq(NoteDocument.class));
        verify(noteMapper).toResponse(doc);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSearchTasks() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("title")));
        Task task = new Task();
        TaskResponse res = TaskResponse.builder().id(UUID.randomUUID()).title("Task").build();

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Task> cq = mock(CriteriaQuery.class);
        Root<Task> root = mock(Root.class);
        TypedQuery<Task> typedQuery = mock(TypedQuery.class);
        Path<Object> userPath = mock(Path.class);
        Path<Object> userIdPath = mock(Path.class);
        Path<Object> titlePath = mock(Path.class);
        Path<Object> priorityPath = mock(Path.class);
        Path<Object> statusPath = mock(Path.class);
        Path<Object> dueDatePath = mock(Path.class);

        // Setup count query mock objects
        CriteriaQuery<Long> countCq = mock(CriteriaQuery.class);
        Root<Task> countRoot = mock(Root.class);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);
        Path<Object> countUserPath = mock(Path.class);
        Path<Object> countUserIdPath = mock(Path.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Task.class)).thenReturn(cq);
        when(cq.from(Task.class)).thenReturn(root);

        // Path navigation mock setups
        when(root.get("user")).thenReturn((Path) userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(root.get("title")).thenReturn((Path) titlePath);
        when(root.get("priority")).thenReturn((Path) priorityPath);
        when(root.get("status")).thenReturn((Path) statusPath);
        when(root.get("dueDate")).thenReturn((Path) dueDatePath);

        Predicate equalUser = mock(Predicate.class);
        when(cb.equal(userIdPath, userId)).thenReturn(equalUser);

        Predicate likeTitle = mock(Predicate.class);
        Expression<String> lowerExpression = mock(Expression.class);
        when(cb.lower(any(Expression.class))).thenReturn(lowerExpression);
        when(cb.like(eq(lowerExpression), eq("%todo%"))).thenReturn(likeTitle);

        Predicate equalPriority = mock(Predicate.class);
        when(cb.equal(priorityPath, TaskPriority.HIGH)).thenReturn(equalPriority);

        Predicate equalStatus = mock(Predicate.class);
        when(cb.equal(statusPath, TaskStatus.PENDING)).thenReturn(equalStatus);

        Predicate betweenDueDate = mock(Predicate.class);
        when(cb.between(any(Expression.class), any(java.time.LocalDateTime.class), any(java.time.LocalDateTime.class))).thenReturn(betweenDueDate);

        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(res);

        // Setup count mock
        when(cb.createQuery(Long.class)).thenReturn(countCq);
        when(countCq.from(Task.class)).thenReturn(countRoot);
        when(countRoot.get("user")).thenReturn((Path) countUserPath);
        when(countUserPath.get("id")).thenReturn(countUserIdPath);
        when(cb.equal(countUserIdPath, userId)).thenReturn(equalUser);
        when(countRoot.get("title")).thenReturn((Path) titlePath);
        when(countRoot.get("priority")).thenReturn((Path) priorityPath);
        when(countRoot.get("status")).thenReturn((Path) statusPath);
        when(countRoot.get("dueDate")).thenReturn((Path) dueDatePath);

        when(countCq.select(any())).thenReturn(countCq);
        when(entityManager.createQuery(countCq)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        Page<TaskResponse> result = searchService.searchTasks(
                userId,
                "TODO",
                TaskPriority.HIGH,
                TaskStatus.PENDING,
                LocalDate.now(),
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(res, result.getContent().get(0));

        verify(entityManager).createQuery(cq);
        verify(entityManager).createQuery(countCq);
        verify(taskMapper).toResponse(task);
    }
}
