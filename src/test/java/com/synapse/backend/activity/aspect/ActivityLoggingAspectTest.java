package com.synapse.backend.activity.aspect;

import com.synapse.backend.activity.model.ActivityType;
import com.synapse.backend.activity.service.ActivityService;
import com.synapse.backend.notes.dto.NoteRequest;
import com.synapse.backend.notes.dto.NoteResponse;
import com.synapse.backend.reminders.model.Reminder;
import com.synapse.backend.reminders.repository.ReminderRepository;
import com.synapse.backend.tasks.dto.TaskRequest;
import com.synapse.backend.tasks.dto.TaskResponse;
import com.synapse.backend.tasks.model.Task;
import com.synapse.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLoggingAspectTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private ReminderRepository reminderRepository;

    @InjectMocks
    private ActivityLoggingAspect aspect;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void testAfterCreateNote() {
        NoteRequest request = new NoteRequest();
        NoteResponse result = NoteResponse.builder()
                .id("note-123")
                .title("Test Title")
                .content("Test Content")
                .build();

        aspect.afterCreateNote(request, userId, result);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(activityService).logActivity(eq(userId), eq(ActivityType.NOTE_CREATED), metadataCaptor.capture());

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals("note-123", metadata.get("noteId"));
        assertEquals("Test Title", metadata.get("title"));
    }

    @Test
    void testAfterUpdateNote() {
        NoteRequest request = new NoteRequest();
        NoteResponse result = NoteResponse.builder()
                .id("note-123")
                .title("Updated Title")
                .content("Test Content")
                .build();

        aspect.afterUpdateNote("note-123", request, userId, result);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(activityService).logActivity(eq(userId), eq(ActivityType.NOTE_UPDATED), metadataCaptor.capture());

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals("note-123", metadata.get("noteId"));
        assertEquals("Updated Title", metadata.get("title"));
    }

    @Test
    void testAfterSetArchived() {
        NoteResponse result = NoteResponse.builder()
                .id("note-123")
                .title("Archived Title")
                .isArchived(true)
                .build();

        aspect.afterSetArchived("note-123", true, userId, result);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(activityService).logActivity(eq(userId), eq(ActivityType.NOTE_ARCHIVED), metadataCaptor.capture());

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals("note-123", metadata.get("noteId"));
        assertEquals("Archived Title", metadata.get("title"));
    }

    @Test
    void testAfterSetArchived_NotArchivedResult() {
        NoteResponse result = NoteResponse.builder()
                .id("note-123")
                .title("Not Archived Title")
                .isArchived(false)
                .build();

        aspect.afterSetArchived("note-123", false, userId, result);

        verifyNoInteractions(activityService);
    }

    @Test
    void testAfterCreateTask() {
        TaskRequest request = new TaskRequest();
        UUID taskId = UUID.randomUUID();
        TaskResponse result = TaskResponse.builder()
                .id(taskId)
                .title("Task Title")
                .build();

        aspect.afterCreateTask(request, userId, result);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(activityService).logActivity(eq(userId), eq(ActivityType.TASK_CREATED), metadataCaptor.capture());

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals(taskId.toString(), metadata.get("taskId"));
        assertEquals("Task Title", metadata.get("title"));
    }

    @Test
    void testAfterCompleteTask() {
        UUID taskId = UUID.randomUUID();
        TaskResponse result = TaskResponse.builder()
                .id(taskId)
                .title("Completed Task Title")
                .build();

        aspect.afterCompleteTask(taskId, userId, result);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(activityService).logActivity(eq(userId), eq(ActivityType.TASK_COMPLETED), metadataCaptor.capture());

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals(taskId.toString(), metadata.get("taskId"));
        assertEquals("Completed Task Title", metadata.get("title"));
    }

    @Test
    void testAfterProcessReminder() {
        UUID reminderId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Reminder Task");
        task.setUser(user);

        Reminder reminder = new Reminder();
        reminder.setId(reminderId);
        reminder.setTask(task);

        when(reminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));

        aspect.afterProcessReminder(reminderId);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(activityService).logActivity(eq(userId), eq(ActivityType.REMINDER_TRIGGERED), metadataCaptor.capture());

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals(reminderId.toString(), metadata.get("reminderId"));
        assertEquals(taskId.toString(), metadata.get("taskId"));
        assertEquals("Reminder Task", metadata.get("taskTitle"));
    }
}
