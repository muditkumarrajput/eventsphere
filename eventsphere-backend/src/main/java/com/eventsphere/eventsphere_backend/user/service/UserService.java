package com.eventsphere.eventsphere_backend.user.service;

import com.eventsphere.eventsphere_backend.common.exception.UserHasEventsException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.user.dto.UpdateUserRequest;
import com.eventsphere.eventsphere_backend.user.dto.UserResponse;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.mapper.UserMapper;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EventRepository eventRepository;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            EventRepository eventRepository) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.eventRepository = eventRepository;
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    public User createUser(User user) {

        return userRepository.save(user);
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // =========================================================
    // GET CURRENT USER
    // =========================================================

    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return userMapper.toResponse(user);
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        return userMapper.toResponse(user);
    }

    // =========================================================
    // UPDATE USER PROFILE
    // =========================================================

    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());
        existingUser.setPhoneNumber(request.getPhoneNumber());

        /*
         * IMPORTANT:
         *
         * Role is intentionally NOT updated here.
         *
         * Role changes must happen through the
         * dedicated ADMIN-only role management endpoint.
         */

        User savedUser =
                userRepository.save(existingUser);

        return userMapper.toResponse(savedUser);
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        // A user cannot be deleted while they still own events.
        if (!eventRepository.findByCreatedBy(user).isEmpty()) {

            throw new UserHasEventsException(id);
        }

        userRepository.delete(user);
    }
}