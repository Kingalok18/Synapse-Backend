package com.synapse.backend.search.controller;

import com.synapse.backend.dto.ApiResponse;
import com.synapse.backend.notes.dto.NoteResponse;
import com.synapse.backend.search.service.SearchService;
import com.synapse.backend.security.UserPrincipal;
import com.synapse.backend.security.jwt.JwtAuthenticationFilter;
import com.synapse.backend.security.oauth.CustomOAuth2UserService;
import com.synapse.backend.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.synapse.backend.security.oauth.OAuth2AuthenticationFailureHandler;
import com.synapse.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.synapse.backend.tasks.dto.TaskResponse;
import com.synapse.backend.tasks.model.TaskPriority;
import com.synapse.backend.tasks.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @MockBean
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserPrincipal userPrincipal;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userPrincipal = new UserPrincipal(
                userId,
                "test@example.com",
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void testSearchNotes() throws Exception {
        NoteResponse noteResponse = NoteResponse.builder()
                .id("note-1")
                .title("Note Title")
                .content("Content")
                .build();
        Page<NoteResponse> page = new PageImpl<>(List.of(noteResponse));

        when(searchService.searchNotes(eq(userId), eq("keyword"), eq("title"), eq("content"), any(), eq(false), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/search/notes")
                        .with(user(userPrincipal))
                        .param("q", "keyword")
                        .param("title", "title")
                        .param("content", "content")
                        .param("recent", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notes search results retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].id").value("note-1"))
                .andExpect(jsonPath("$.data.content[0].title").value("Note Title"));
    }

    @Test
    void testSearchTasks() throws Exception {
        TaskResponse taskResponse = TaskResponse.builder()
                .id(UUID.randomUUID())
                .title("Task Title")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.PENDING)
                .build();
        Page<TaskResponse> page = new PageImpl<>(List.of(taskResponse));

        when(searchService.searchTasks(eq(userId), eq("Task"), eq(TaskPriority.HIGH), eq(TaskStatus.PENDING), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/search/tasks")
                        .with(user(userPrincipal))
                        .param("title", "Task")
                        .param("priority", "HIGH")
                        .param("status", "PENDING")
                        .param("dueDate", LocalDate.now().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tasks search results retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].title").value("Task Title"))
                .andExpect(jsonPath("$.data.content[0].priority").value("HIGH"));
    }
}
