package com.synapse.backend.analytics.controller;

import com.synapse.backend.analytics.dto.AnalyticsResponseDTO;
import com.synapse.backend.analytics.service.AnalyticsService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

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
    void testGetAnalytics() throws Exception {
        AnalyticsResponseDTO response = AnalyticsResponseDTO.builder()
                .notesCreatedCount(12)
                .notesArchivedCount(3)
                .tasksCompletedCount(15)
                .tasksPendingCount(5)
                .upcomingReminderCount(4)
                .recentActivity(Collections.emptyList())
                .build();

        when(analyticsService.getAnalytics(eq(userId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics")
                        .with(user(userPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Analytics data retrieved successfully"))
                .andExpect(jsonPath("$.data.notesCreatedCount").value(12))
                .andExpect(jsonPath("$.data.notesArchivedCount").value(3))
                .andExpect(jsonPath("$.data.tasksCompletedCount").value(15))
                .andExpect(jsonPath("$.data.tasksPendingCount").value(5))
                .andExpect(jsonPath("$.data.upcomingReminderCount").value(4));
    }
}
