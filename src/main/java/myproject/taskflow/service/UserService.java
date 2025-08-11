package myproject.taskflow.service;

import myproject.taskflow.dto.request.UserUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.UserDetailResponse;
import myproject.taskflow.dto.response.UserSummaryResponse;

import java.util.List;

public interface UserService {
    List<UserSummaryResponse> getAllUsers();   // admin manager
    UserDetailResponse getUserById(Long userId);
    SimpleResponse updateOwnProfile(UserUpdateRequest userUpdateRequest);
    SimpleResponse deleteUserById(Long userId);  // for admin only
}
