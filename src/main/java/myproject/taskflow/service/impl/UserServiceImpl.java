package myproject.taskflow.service.impl;

import org.springframework.transaction.annotation.Transactional;

import myproject.taskflow.dto.request.UserUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.UserDetailResponse;
import myproject.taskflow.dto.response.UserSummaryResponse;
import myproject.taskflow.entities.User;
import myproject.taskflow.repositories.jdbc.UserJdbcRepository;
import myproject.taskflow.repositories.jpa.RefreshTokenRepository;
import myproject.taskflow.repositories.jpa.TeamMemberRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import myproject.taskflow.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserJdbcRepository userJdbcRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TeamMemberRepository teamMemberRepository;

    public UserServiceImpl(UserJdbcRepository userJdbcRepository, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, TeamMemberRepository teamMemberRepository) {
        this.userJdbcRepository = userJdbcRepository;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        return userJdbcRepository.getAllUsers();
    }

    @Override
    public UserDetailResponse getUserById(Long userId) {
        return userJdbcRepository.getUserById(userId);
    }

    @Override
    public SimpleResponse updateOwnProfile(UserUpdateRequest userUpdateRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findUserByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        currentUser.setNickname(userUpdateRequest.getNickname());
        currentUser.setFirstName(userUpdateRequest.getFirstName());
        currentUser.setLastName(userUpdateRequest.getLastName());
        currentUser.setEmail(email);
        userRepository.save(currentUser);
        return new SimpleResponse("Profile updated!", HttpStatus.OK);
    }
    @Transactional
    @Override
    public SimpleResponse deleteUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUserId(userId);
        teamMemberRepository.deleteByUserId(userId);
        userRepository.delete(user);

        return new SimpleResponse("User deleted!", HttpStatus.OK);
    }
}
