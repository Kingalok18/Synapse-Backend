package com.synapse.backend.analytics.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.backend.activity.dto.ActivityResponseDTO;
import com.synapse.backend.activity.mapper.ActivityMapper;
import com.synapse.backend.activity.model.Activity;
import com.synapse.backend.activity.repository.ActivityRepository;
import com.synapse.backend.analytics.dto.AnalyticsResponseDTO;
import com.synapse.backend.notes.document.NoteDocument;
import com.synapse.backend.tasks.model.TaskStatus;
import com.synapse.backend.tasks.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private UUID userId;
    private String cacheKey;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cacheKey = "synapse:analytics:stats:" + userId;
    }

    @Test
    void testGetAnalytics_CacheHit() throws Exception {
        AnalyticsResponseDTO cachedDTO = AnalyticsResponseDTO.builder()
                .notesCreatedCount(5)
                .notesArchivedCount(1)
                .tasksCompletedCount(10)
                .tasksPendingCount(2)
                .upcomingReminderCount(3)
                .recentActivity(Collections.emptyList())
                .build();
        String json = objectMapper.writeValueAsString(cachedDTO);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(json);

        AnalyticsResponseDTO result = analyticsService.getAnalytics(userId);

        assertNotNull(result);
        assertEquals(5, result.getNotesCreatedCount());
        assertEquals(1, result.getNotesArchivedCount());
        assertEquals(10, result.getTasksCompletedCount());
        assertEquals(2, result.getTasksPendingCount());
        assertEquals(3, result.getUpcomingReminderCount());
        assertTrue(result.getRecentActivity().isEmpty());

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(cacheKey);
        verifyNoInteractions(mongoTemplate, taskRepository, activityRepository, entityManager);
    }

    @Test
    void testGetAnalytics_CacheMiss() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(null);

        // Mock Note counts
        when(mongoTemplate.count(any(Query.class), eq(NoteDocument.class))).thenReturn(5L).thenReturn(2L);

        // Mock Task counts
        when(taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED)).thenReturn(10L);
        when(taskRepository.countByUserIdAndStatus(userId, TaskStatus.PENDING)).thenReturn(4L);

        // Mock Upcoming reminders
        TypedQuery<Long> queryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(queryMock);
        when(queryMock.setParameter(anyString(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(3L);

        // Mock Activity list
        Activity activity = new Activity();
        when(activityRepository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));
        ActivityResponseDTO activityDTO = new ActivityResponseDTO();
        when(activityMapper.toResponse(activity)).thenReturn(activityDTO);

        AnalyticsResponseDTO result = analyticsService.getAnalytics(userId);

        assertNotNull(result);
        assertEquals(5, result.getNotesCreatedCount());
        assertEquals(2, result.getNotesArchivedCount());
        assertEquals(10, result.getTasksCompletedCount());
        assertEquals(4, result.getTasksPendingCount());
        assertEquals(3, result.getUpcomingReminderCount());
        assertEquals(1, result.getRecentActivity().size());

        verify(mongoTemplate, times(2)).count(any(Query.class), eq(NoteDocument.class));
        verify(taskRepository).countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
        verify(taskRepository).countByUserIdAndStatus(userId, TaskStatus.PENDING);
        verify(entityManager).createQuery(anyString(), eq(Long.class));
        verify(activityRepository).findByUserId(eq(userId), any(Pageable.class));

        // Verify cache write
        verify(valueOperations).set(eq(cacheKey), anyString(), eq(1L), eq(TimeUnit.HOURS));
    }
}
