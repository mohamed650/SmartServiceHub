package com.ssh.smartServiceHub.service;

import com.ssh.smartServiceHub.dto.RegisterRequest;
import com.ssh.smartServiceHub.dto.UserDTO;
import com.ssh.smartServiceHub.entity.User;
import com.ssh.smartServiceHub.mapper.UserMapper;
import com.ssh.smartServiceHub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Mohamed Khalid")
                .email("mohamed@gmail.com")
                .password("mohamed")
                .role("USER")
                .build();

        testUserDTO = UserDTO.builder()
                .id(1L)
                .fullName("Mohamed Khalid")
                .email("mohamed@gmail.com")
                .password("mohamed1")
                .role("USER")
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsersDTOs_WhenUsersExist() {
        User secondUser = User.builder()
                .id(2L)
                .fullName("Bhanu Priya")
                .email("priya@gmail.com")
                .password("priya")
                .role("ADMIN")
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, secondUser));

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFullName()).isEqualTo("Mohamed Khalid");
        assertThat(result.get(0).getEmail()).isEqualTo("mohamed@gmail.com");
        assertThat(result.get(1).getFullName()).isEqualTo("Bhanu Priya");
        assertThat(result.get(1).getRole()).isEqualTo("ADMIN");

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).isEmpty();

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Mohamed Khalid");
        assertThat(result.getEmail()).isEqualTo("mohamed@gmail.com");
        assertThat(result.getRole()).isEqualTo("USER");

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowRuntimeException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(999L);
    }

    @Test
    void createUser_ShouldReturnCreatedUserDTO_WhenValidUserProvided() {
        when(passwordEncoder.encode("mohamed1")).thenReturn("mohamed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.createUser(testUserDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Mohamed Khalid");
        assertThat(result.getEmail()).isEqualTo("mohamed@gmail.com");

        verify(passwordEncoder).encode("mohamed1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldEncodePassword_WhenCreatingUser() {
        when(passwordEncoder.encode("mohamed1")).thenReturn("mohamed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getPassword()).isEqualTo("mohamed");
            return testUser;
        });

        userService.createUser(testUserDTO);

        verify(passwordEncoder).encode("mohamed");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDTO_WhenUserExists() {
        UserDTO updatedUserDTO = UserDTO.builder()
                .fullName("Mohamed Khalid Upd")
                .email("mohamed@example.com")
                .password("khalid")
                .role("ADMIN")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .fullName("Mohamed Khalid Upd")
                .email("mohamed@example.com")
                .password("khalid1")
                .role("ADMIN")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("khalid")).thenReturn("khalid1");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDTO result = userService.updateUser(1L, updatedUserDTO);

        assertThat(result.getFullName()).isEqualTo("Mohamed Khalid Upd");
        assertThat(result.getEmail()).isEqualTo("mohamed@gmail.com");
        assertThat(result.getRole()).isEqualTo("ADMIN");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("khalid");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowRuntimeException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, testUserDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete_WhenIdProvided() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void findByUsername_ShouldReturnUserDTO_WhenEmailExists() {
        when(userRepository.findByEmail("mohamed@gmail.com")).thenReturn(Optional.of(testUser));

        UserDTO result = userService.findByUsername("mohamed@gmail.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("mohamed@gmail.com");

        verify(userRepository).findByEmail("mohamed@gmail.com");
    }

    @Test
    void findByUsername_ShouldThrowRuntimeException_WhenEmailNotFound() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("notfound@gmail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail("notfound@gmail.com");
    }
}
