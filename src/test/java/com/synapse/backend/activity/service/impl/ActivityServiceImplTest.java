package com.synapse.backend.activity.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.backend.activity.dto.ActivityResponseDTO;
import com.synapse.backend.activity.mapper.ActivityMapper;
import com.synapse.backend.activity.model.Activity;
import com.synapse.backend.activity.model.ActivityType;
import com.synapse.backend.activity.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityMapper activityMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void testLogActivityWithMetadata() throws Exception {
        Map<String, Object> metadata = Map.of("key", "value");
        ActivityType type = ActivityType.NOTE_CREATED;
        String cacheKey = "synapse:analytics:stats:" + userId;

        activityService.logActivity(userId, type, metadata);

        // Verify save is called
        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(activityCaptor.capture());

        Activity savedActivity = activityCaptor.getValue();
        assertEquals(userId, savedActivity.getUserId());
        assertEquals(type, savedActivity.getType());
        assertNotNull(savedActivity.getMetadata());
        assertTrue(savedActivity.getMetadata().contains("\"key\":\"value\""));
        assertNotNull(savedActivity.getCreatedAt());

        // Verify Redis eviction
        verify(redisTemplate).delete(cacheKey);
    }

    @Test
    void testLogActivityNoMetadata() {
        ActivityType type = ActivityType.TASK_COMPLETED;
        String cacheKey = "synapse:analytics:stats:" + userId;

        activityService.logActivity(userId, type, null);

        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(activityCaptor.capture());

        Activity savedActivity = activityCaptor.getValue();
        assertEquals(userId, savedActivity.getUserId());
        assertEquals(type, savedActivity.getType());
        assertNull(savedActivity.getMetadata());

        verify(redisTemplate).delete(cacheKey);
    }

    @Test
    void testGetActivities() {
        Pageable pageable = PageRequest.of(0, 10);
        Activity activity = new Activity();
        Page<Activity> activityPage = new PageImpl<>(List.of(activity), pageable, 1);

        when(activityRepository.findByUserId(userId, pageable)).thenReturn(activityPage);

        ActivityResponseDTO responseDTO = new ActivityResponseDTO();
        when(activityMapper.toResponse(activity)).thenReturn(responseDTO);

        Page<ActivityResponseDTO> result = activityService.getActivities(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(responseDTO, result.getContent().get(0));
        verify(activityRepository).findByUserId(userId, pageable);
        verify(activityMapper).toResponse(activity);
    }
}
