package myproject.taskflow.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import myproject.taskflow.entities.User;
import myproject.taskflow.repositories.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
@Service
public class JwtService {
    @Value("${security.secret.key}")
    private String secretKey;

    private final UserRepository userRepo;

    @Autowired
    public JwtService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public String generateToken(User user) {
        return JWT.create()
                .withClaim("id", user.getId())
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(ZonedDateTime.now().toInstant())
                .withExpiresAt(ZonedDateTime.now().plusMinutes(15).toInstant())
                .sign(Algorithm.HMAC256(secretKey));
    }



    public User verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String email = decodedJWT.getClaim("email").asString();

        return userRepo.findUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
