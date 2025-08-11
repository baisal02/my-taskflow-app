package myproject.taskflow.api;

import myproject.taskflow.dto.request.UserUpdateRequest;
import myproject.taskflow.dto.response.SimpleResponse;
import myproject.taskflow.dto.response.UserDetailResponse;
import myproject.taskflow.dto.response.UserSummaryResponse;
import myproject.taskflow.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserApi {
    private final UserService userService;

    public UserApi(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all users in summary form.")
    public List<UserSummaryResponse> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns detailed information of a user identified by their ID.")
    public UserDetailResponse getUser(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping
    @Operation(summary = "Update own profile", description = "Allows the currently authenticated user to update their own profile information.")
    public SimpleResponse updateUser(
            @Parameter(description = "User data to update") @RequestBody UserUpdateRequest userUpdateRequest) {
        return userService.updateOwnProfile(userUpdateRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID", description = "Allows an admin to delete a user identified by their ID.")
    public SimpleResponse deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Long id) {
        return userService.deleteUserById(id);
    }
}
