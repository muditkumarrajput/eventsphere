package com.eventsphere.eventsphere_backend.user.service;

import com.eventsphere.eventsphere_backend.common.exception.UserEmailAlreadyExistsException;
import com.eventsphere.eventsphere_backend.common.exception.UserHasEventsException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.user.dto.ChangeUserRoleRequest;
import com.eventsphere.eventsphere_backend.user.dto.UpdateUserRequest;
import com.eventsphere.eventsphere_backend.user.dto.UserResponse;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.mapper.UserMapper;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private UserService userService;


    // =========================================================
    // CREATE USER
    // =========================================================

    @Test
    void shouldCreateUser() {

        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@test.com");

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.createUser(user);

        assertEquals(user, result);

        verify(userRepository).save(user);
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Test
    void shouldGetAllUsers() {

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@test.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");

        UserResponse response1 = UserResponse.builder()
                .id(1L)
                .email("user1@test.com")
                .build();

        UserResponse response2 = UserResponse.builder()
                .id(2L)
                .email("user2@test.com")
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        when(userMapper.toResponse(user1))
                .thenReturn(response1);

        when(userMapper.toResponse(user2))
                .thenReturn(response2);

        List<UserResponse> result =
                userService.getAllUsers();

        assertEquals(2, result.size());

        assertEquals(
                1L,
                result.get(0).getId()
        );

        assertEquals(
                2L,
                result.get(1).getId()
        );

        assertEquals(
                "user1@test.com",
                result.get(0).getEmail()
        );

        assertEquals(
                "user2@test.com",
                result.get(1).getEmail()
        );

        verify(userRepository).findAll();
        verify(userMapper).toResponse(user1);
        verify(userMapper).toResponse(user2);
    }


    // =========================================================
    // GET CURRENT USER
    // =========================================================

    @Test
    void shouldGetCurrentUser() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        UserResponse response = UserResponse.builder()
                .id(5L)
                .email(email)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.getCurrentUser(email);

        assertEquals(5L, result.getId());

        assertEquals(
                email,
                result.getEmail()
        );

        verify(userRepository)
                .findByEmail(email);

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldThrowExceptionWhenCurrentUserDoesNotExist() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> userService.getCurrentUser(email)
                );

        assertEquals(
                "User with email unknown@test.com not found.",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(userMapper);
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Test
    void shouldGetUserById() {

        Long id = 5L;

        User user = new User();
        user.setId(id);
        user.setEmail("user@test.com");

        UserResponse response = UserResponse.builder()
                .id(id)
                .email("user@test.com")
                .build();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.getUserById(id);

        assertEquals(
                id,
                result.getId()
        );

        assertEquals(
                "user@test.com",
                result.getEmail()
        );

        verify(userRepository)
                .findById(id);

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldThrowExceptionWhenUserByIdDoesNotExist() {

        Long id = 999L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> userService.getUserById(id)
                );

        assertEquals(
                "User with id " + id + " not found.",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(id);

        verifyNoInteractions(userMapper);
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Test
    void shouldUpdateUserProfile() {

        String oldEmail = "old@test.com";
        String newEmail = "new@test.com";

        User user = new User();
        user.setId(5L);
        user.setName("Old Name");
        user.setEmail(oldEmail);
        user.setPhoneNumber("1111111111");

        UpdateUserRequest request =
                new UpdateUserRequest();

        request.setName("New Name");
        request.setEmail(newEmail);
        request.setPhoneNumber("9999999999");

        UserResponse response = UserResponse.builder()
                .id(5L)
                .email(newEmail)
                .build();

        when(userRepository.findByEmail(oldEmail))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail(newEmail))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.updateUser(
                        oldEmail,
                        request
                );

        assertEquals(
                "New Name",
                user.getName()
        );

        assertEquals(
                newEmail,
                user.getEmail()
        );

        assertEquals(
                "9999999999",
                user.getPhoneNumber()
        );

        assertEquals(
                5L,
                result.getId()
        );

        assertEquals(
                newEmail,
                result.getEmail()
        );

        verify(userRepository)
                .findByEmail(oldEmail);

        verify(userRepository)
                .existsByEmail(newEmail);

        verify(userRepository)
                .save(user);

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldUpdateUserWhenEmailRemainsSame() {

        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setName("Old Name");
        user.setEmail(email);
        user.setPhoneNumber("1111111111");

        UpdateUserRequest request =
                new UpdateUserRequest();

        request.setName("New Name");
        request.setEmail(email);
        request.setPhoneNumber("9999999999");

        UserResponse response = UserResponse.builder()
                .id(5L)
                .email(email)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.updateUser(
                        email,
                        request
                );

        assertEquals(
                "New Name",
                user.getName()
        );

        assertEquals(
                email,
                user.getEmail()
        );

        assertEquals(
                "9999999999",
                user.getPhoneNumber()
        );

        assertEquals(
                email,
                result.getEmail()
        );

        // existsByEmail should NOT be called
        verify(
                userRepository,
                never()
        ).existsByEmail(email);

        verify(userRepository)
                .save(user);

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldRejectUpdateWhenEmailAlreadyExists() {

        String email = "user@test.com";
        String existingEmail = "another@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        UpdateUserRequest request =
                new UpdateUserRequest();

        request.setName("Updated Name");
        request.setEmail(existingEmail);
        request.setPhoneNumber("9999999999");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail(existingEmail))
                .thenReturn(true);

        UserEmailAlreadyExistsException exception =
                assertThrows(
                        UserEmailAlreadyExistsException.class,
                        () -> userService.updateUser(
                                email,
                                request
                        )
                );

        assertEquals(
                "User with email '" +
                        existingEmail +
                        "' already exists",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(email);

        verify(userRepository)
                .existsByEmail(existingEmail);

        verify(
                userRepository,
                never()
        ).save(any(User.class));

        verifyNoInteractions(userMapper);
    }


    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {

        String email = "unknown@test.com";

        UpdateUserRequest request =
                new UpdateUserRequest();

        request.setName("New Name");
        request.setEmail("new@test.com");
        request.setPhoneNumber("9999999999");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(
                        email,
                        request
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(
                userRepository,
                never()
        ).save(any(User.class));

        verifyNoInteractions(userMapper);
    }


    // =========================================================
    // CHANGE USER ROLE
    // =========================================================

    @Test
    void shouldChangeUserRole() {

        Long id = 5L;

        User user = new User();
        user.setId(id);
        user.setRole(Role.USER);

        ChangeUserRoleRequest request =
                new ChangeUserRoleRequest();

        request.setRole(Role.ORGANIZER);

        UserResponse response = UserResponse.builder()
                .id(id)
                .build();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.changeUserRole(
                        id,
                        request
                );

        assertEquals(
                Role.ORGANIZER,
                user.getRole()
        );

        assertEquals(
                id,
                result.getId()
        );

        verify(userRepository)
                .findById(id);

        verify(userRepository)
                .save(user);

        verify(userMapper)
                .toResponse(user);
    }


    @Test
    void shouldThrowExceptionWhenChangingRoleForNonExistingUser() {

        Long id = 999L;

        ChangeUserRoleRequest request =
                new ChangeUserRoleRequest();

        request.setRole(Role.ORGANIZER);

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.changeUserRole(
                        id,
                        request
                )
        );

        verify(userRepository)
                .findById(id);

        verify(
                userRepository,
                never()
        ).save(any(User.class));

        verifyNoInteractions(userMapper);
    }


    // =========================================================
    // DELETE USER
    // =========================================================

    @Test
    void shouldDeleteUserWhenUserHasNoEvents() {

        Long id = 5L;

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByCreatedBy(user))
                .thenReturn(List.of());

        userService.deleteUser(id);

        verify(userRepository)
                .findById(id);

        verify(eventRepository)
                .findByCreatedBy(user);

        verify(userRepository)
                .delete(user);
    }


    @Test
    void shouldRejectDeleteWhenUserOwnsEvents() {

        Long id = 5L;

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByCreatedBy(user))
                .thenReturn(
                        List.of(mock(Event.class))
                );

        UserHasEventsException exception =
                assertThrows(
                        UserHasEventsException.class,
                        () -> userService.deleteUser(id)
                );

        assertEquals(
                "User with id " +
                        id +
                        " cannot be deleted because they still own events.",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(id);

        verify(eventRepository)
                .findByCreatedBy(user);

        verify(
                userRepository,
                never()
        ).delete(any(User.class));
    }


    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUser() {

        Long id = 999L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(id)
        );

        verify(userRepository)
                .findById(id);

        verifyNoInteractions(eventRepository);
    }
}