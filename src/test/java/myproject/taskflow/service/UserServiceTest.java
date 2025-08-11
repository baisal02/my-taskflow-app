package myproject.taskflow.service;

import myproject.taskflow.dto.request.UserUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.UserDetailResponse;
import myproject.taskflow.dto.response.UserSummaryResponse;
import myproject.taskflow.entities.User;
import myproject.taskflow.repositories.jdbc.UserJdbcRepository;
import myproject.taskflow.repositories.jpa.RefreshTokenRepository;
import myproject.taskflow.repositories.jpa.TeamMemberRepository;
import myproject.taskflow.repositories.jpa.UserRepository;
import myproject.taskflow.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserJdbcRepository userJdbcRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private void mockSecurityContext(String email) {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllUsers_returnsList() {
        List<UserSummaryResponse> mockList = List.of(
                new UserSummaryResponse(1L, "nick1"),
                new UserSummaryResponse(2L, "nick2")
        );
        when(userJdbcRepository.getAllUsers()).thenReturn(mockList);

        List<UserSummaryResponse> result = userService.getAllUsers();

        assertEquals(mockList, result);
        verify(userJdbcRepository).getAllUsers();
    }

    @Test
    void getUserById_returnsUserDetail() {
        UserDetailResponse mockUser = new UserDetailResponse(
                1L,
                "nick1",
                "user1@test.com",
                "John",
                LocalDateTime.of(2023, 8, 10, 12, 0)
        );
        when(userJdbcRepository.getUserById(1L)).thenReturn(mockUser);

        UserDetailResponse result = userService.getUserById(1L);

        assertEquals(mockUser, result);
        verify(userJdbcRepository).getUserById(1L);
    }

    @Test
    void updateOwnProfile_success() {
        String email = "user@test.com";
        UserUpdateRequest req = new UserUpdateRequest("nick", "First", "Last","email");

        User currentUser = new User();
        currentUser.setEmail(email);

        mockSecurityContext(email);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        SimpleResponse response = userService.updateOwnProfile(req);

        assertEquals("Profile updated!", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(userRepository).save(currentUser);
        assertEquals("nick", currentUser.getNickname());
        assertEquals("First", currentUser.getFirstName());
        assertEquals("Last", currentUser.getLastName());
    }

    @Test
    void updateOwnProfile_userNotFound_throws() {
        String email = "user@test.com";
        UserUpdateRequest req = new UserUpdateRequest("nick", "First", "Last","email");

        mockSecurityContext(email);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateOwnProfile(req));
    }

    @Test
    void deleteUserById_success() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        SimpleResponse response = userService.deleteUserById(1L);

        assertEquals("User deleted!", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());

        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(teamMemberRepository).deleteByUserId(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserById_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.deleteUserById(1L));
    }


}
