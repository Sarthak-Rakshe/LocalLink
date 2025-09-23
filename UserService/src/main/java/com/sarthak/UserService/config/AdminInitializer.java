package com.sarthak.UserService.config;

import com.sarthak.UserService.dto.AdminProperties;
import com.sarthak.UserService.model.User;
import com.sarthak.UserService.model.UserRole;
import com.sarthak.UserService.model.UserType;
import com.sarthak.UserService.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createDefaultAdmin(UserRepository userRepository,
                                         PasswordEncoder passwordEncoder,
                                         AdminProperties adminProperties){
        return args  ->{
            userRepository.findByUsername(adminProperties.getUsername())
                    .ifPresentOrElse(
                            user -> System.out.println("Admin user already exists with username: " + adminProperties.getUsername()),
                            () -> {
                                User admin = new User();
                                admin.setUsername(adminProperties.getUsername());
                                admin.setUserPassword(passwordEncoder.encode(adminProperties.getPassword()));
                                admin.setUserType(UserType.PROVIDER);
                                admin.setUserRole(UserRole.ADMIN);
                                admin.setUserEmail(adminProperties.getEmail());
                                admin.setUserContact("9856472310");
                                admin.setUserAddress("Default Address");

                                userRepository.save(admin);
                                System.out.println("Default admin user created with username: " + adminProperties.getUsername());
                            }
                    );
        }    ;
    }
}
