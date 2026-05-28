package com.synapse.backend.service;

import com.synapse.backend.dto.response.UserResponse;
import com.synapse.backend.exception.ResourceNotFoundException;
import com.synapse.backend.model.User;
import com.synapse.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserResponse(user);
    }

    public UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }
}
