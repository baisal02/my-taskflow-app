package myproject.taskflow.init;

import myproject.taskflow.entities.User;
import myproject.taskflow.enums.Role;
import myproject.taskflow.repositories.jpa.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.count() == 0) {
        User manager = new User();
        manager.setEmail("admin");
        manager.setPassword(passwordEncoder.encode("admin"));
        manager.setFirstName("Adminbek");
        manager.setLastName("Adminbekov");
        manager.setRole(Role.ADMIN);
        manager.setNickname("adminYo");
        userRepository.save(manager);
        }
    }
}
