package com.devteria.profile.service;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.entity.UserProfile;
import com.devteria.profile.mapper.UserProfileMapper;
import com.devteria.profile.repository.UserProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRepositoryService {

    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;

    public UserProfileResponse createUser(ProfileCreationRequest request) {
        UserProfile entity = userProfileMapper.toUserProfile(request);
        entity = userProfileRepository.save(entity);
        return userProfileMapper.toUserProfileResponse(entity);
    }

    public UserProfileResponse getUserProfile(String id) {
        UserProfile entity = userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found profile"));
        return userProfileMapper.toUserProfileResponse(entity);
    }

    public List<UserProfileResponse> getAllProfiles() {
        return userProfileRepository.findAll().stream().map(userProfileMapper::toUserProfileResponse).toList();
    }
}
