package myproject.taskflow.service;

import myproject.taskflow.dto.response.RefreshTokenResponse;
import myproject.taskflow.entities.User;

public interface RefreshTokenService {
   String createRefreshToken(User user);

}
