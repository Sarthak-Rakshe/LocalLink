package com.sarthak.UserService.service;

import com.sarthak.UserService.client.ReviewServiceClient;
import com.sarthak.UserService.config.PasswordEncoderConfig;
import com.sarthak.UserService.dto.ProviderReviewAggregateResponse;
import com.sarthak.UserService.dto.request.UserUpdateRequest;
import com.sarthak.UserService.dto.response.ProviderResponse;
import com.sarthak.UserService.exception.AlreadyInUseException;
import com.sarthak.UserService.exception.InvalidUserTypeException;
import com.sarthak.UserService.exception.PasswordCannotBeNullException;
import com.sarthak.UserService.exception.UserNotFoundException;
import com.sarthak.UserService.mapper.UserMapper;
import com.sarthak.UserService.model.User;
import com.sarthak.UserService.model.UserPrincipal;
import com.sarthak.UserService.model.UserRole;
import com.sarthak.UserService.model.UserType;
import com.sarthak.UserService.repository.UserRepository;
import com.sarthak.UserService.dto.request.UserRegistrationRequest;
import com.sarthak.UserService.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoderConfig passwordEncoderConfig;
    private final ReviewServiceClient reviewServiceClient;
    private final Set<String> ALLOWED_SORT_FIELDS = Set.of("userId", "username", "userEmail", "userContact", "userType");


    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username){
        User user = userRepository.findByUsernameOrUserEmailOrUserContact(username, username, username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username, email or contact: " + username));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir){
        Pageable pageable = getPageable(page, size, sortBy, sortDir);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User does not exist with id: " + id));

        log.info("Updating user with id: {}", id);

        Optional.ofNullable(request.username()).ifPresent(name -> {
            if(!name.equals(user.getUsername()) && userRepository.existsByUsername(name)){
                throw new AlreadyInUseException("Username is already in use");
            }
            user.setUsername(name);
        });

        Optional.ofNullable(request.userEmail()).ifPresent(email -> {
            if(!email.equals(user.getUserEmail()) && userRepository.existsByUserEmail(email)){
                throw new AlreadyInUseException("Email is already in use");
            }
            user.setUserEmail(email);
        });

        Optional.ofNullable(request.userContact()).ifPresent(contact -> {
            if(!contact.equals(user.getUserContact()) && userRepository.existsByUserContact(contact)){
                throw new AlreadyInUseException("Contact is already in use");
            }
            user.setUserContact(contact);
        });


        Optional.ofNullable(request.userAddress()).ifPresent(user::setUserAddress);

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getUserId());

        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public UserResponse deactivateUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User does not exist with id: " + id));

        user.setIsActive(false);
        User updatedUser = userRepository.save(user);
        log.info("User deactivated successfully with id: {}", updatedUser.getUserId());

        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public UserResponse activateUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User does not exist with id: " + id));

        user.setIsActive(true);
        User updatedUser = userRepository.save(user);
        log.info("User activated successfully with id: {}", updatedUser.getUserId());

        return userMapper.toResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username){
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email){
        return userRepository.existsByUserEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByContact(String contact){
        return userRepository.existsByUserContact(contact);
    }

    @Transactional
    public void deleteUserById(Long userId){
        if(!userRepository.existsById(userId)){
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted successfully with id: {}", userId);
    }

    public UserResponse registerUser(UserRegistrationRequest request){
        String userName = request.getUserName().toLowerCase();
        String email = request.getUserEmail();
        String contact = request.getUserContact();
        String password = request.getUserPassword();
        String type = request.getUserType().toUpperCase();

        log.info("Registering user with username: {}, email: {}, contact: {}, type: {}", userName, email, contact, type);

        validateDetails(userName,  email, contact, password);

        User user = User.builder()
                .username(userName)
                .userEmail(email)
                .userContact(contact)
                .userPassword(passwordEncoderConfig.passwordEncoder().encode(password))
                .userType(fromString(type))
                .userRole(UserRole.USER)
                .userAddress(request.getUserAddress())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with username: {}", savedUser.getUsername());

        return userMapper.toResponse(savedUser);
    }


    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrUserEmailOrUserContact(username, username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username, email or contact: " + username));

        return new UserPrincipal(user);
    }

    private Pageable getPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10; // default page size
        if(sortBy == null || sortBy.isEmpty()) sortBy = "userId";
        String sortDirNormalized = (sortDir != null) ? sortDir.toLowerCase() : "desc";

        if (!sortDirNormalized.equals("asc") && !sortDirNormalized.equals("desc")) {
            sortDirNormalized = "desc"; // default to descending if invalid
        }

        String sortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "userId";

        Sort sort = sortDirNormalized.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        return PageRequest.of(page, size, sort);
    }

    private void validateDetails(String userName, String userEmail,
                                 String userContact, String password) {
        if (userName != null && userRepository.existsByUsername(userName)) {
            throw new AlreadyInUseException("Username is already in use or is Null");
        }
        if (userEmail != null && userRepository.existsByUserEmail(userEmail)) {
            throw new AlreadyInUseException("Email is already in use or is Null");
        }
        if (userContact != null && userRepository.existsByUserContact(userContact)) {
            throw new AlreadyInUseException("Contact number is already in use or is Null");
        }
        if (password == null){
            throw new PasswordCannotBeNullException("Password cannot be null");
        }

    }

    private UserType fromString(String type){
        UserType userType;
        try{
            userType = UserType.valueOf(type);
        }catch (IllegalArgumentException e){
            throw new InvalidUserTypeException("Invalid user type: " + type);
        }

        return userType;
    }

    public Page<ProviderResponse> getProviders(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching providers - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        Pageable pageable = getPageable(page, size, sortBy, sortDir);
        Page<User> providers = userRepository.findAllByUserTypeAndUserRole(UserType.PROVIDER, UserRole.USER,
                pageable);
        log.info("Found {} providers", providers.getTotalElements());
        List<Long> providerIds = providers.stream()
                .map(User::getUserId)
                .toList();
        Map<Long, ProviderReviewAggregateResponse> reviewAggregates =
                reviewServiceClient.getProviderReviewAggregates(providerIds);
        log.info("Fetched review aggregates for {} providers", reviewAggregates.size());
        List<ProviderResponse> resultMap = new ArrayList<>();
        for(User provider : providers){
            ProviderReviewAggregateResponse aggregate = reviewAggregates.get(provider.getUserId());
            log.info("Provider ID: {}, Review Aggregate: {}", provider.getUserId(), aggregate);
            resultMap.add(userMapper.toProviderResponse(provider, aggregate));
        }
        log.info("Mapped {} providers to ProviderResponse", resultMap.size());
        return new PageImpl<>(resultMap, pageable, providers.getTotalElements());
    }
}
