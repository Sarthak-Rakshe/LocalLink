package com.sarthak.UserService.repository;

import com.sarthak.UserService.model.User;
import com.sarthak.UserService.model.UserRole;
import com.sarthak.UserService.model.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByUsername(String username);
    boolean existsByUserEmail(String userEmail);
    boolean existsByUserContact(String userContact);

    Optional<User> findByUsername(String username);
    Optional<User> findByUserEmail(String userEmail);
    Optional<User> findByUserContact(String userContact);

    @Query("SELECT u FROM User u WHERE u.username = ?1 OR u.userEmail = ?2 OR u.userContact = ?3")
    Optional<User> findByUsernameOrUserEmailOrUserContact(String username, String userEmail, String userContact);

    Page<User> findAllByIsActiveTrue(Pageable pageable);

    Optional<User> findByUserIdAndUserType(Long providerId, UserType userType);
}
