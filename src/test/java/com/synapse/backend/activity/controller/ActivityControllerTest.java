package com.synapse.backend.activity.controller;

import com.synapse.backend.activity.dto.ActivityResponseDTO;
import com.synapse.backend.activity.model.ActivityType;
import com.synapse.backend.activity.service.ActivityService;
import com.synapse.backend.security.UserPrincipal;
import com.synapse.backend.security.jwt.JwtAuthenticationFilter;
import com.synapse.backend.security.oauth.CustomOAuth2UserService;
import com.synapse.backend.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.synapse.backend.security.oauth.OAuth2AuthenticationFailureHandler;
import com.synapse.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

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
    void testGetActivities() throws Exception {
        ActivityResponseDTO activityDTO = ActivityResponseDTO.builder()
                .type(ActivityType.NOTE_CREATED)
                .metadata(Collections.singletonMap("noteId", "note-123"))
                .build();
        Page<ActivityResponseDTO> page = new PageImpl<>(List.of(activityDTO));

        when(activityService.getActivities(eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/activity")
                        .with(user(userPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Activity logs retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].type").value("NOTE_CREATED"))
                .andExpect(jsonPath("$.data.content[0].metadata.noteId").value("note-123"));
    }
}
