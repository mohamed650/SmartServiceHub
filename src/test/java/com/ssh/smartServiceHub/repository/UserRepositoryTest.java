package com.ssh.smartServiceHub.repository;

import com.ssh.smartServiceHub.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .fullName("Mohamed Khalid")
                .email("mohamed@gmail.com")
                .password("mohamed")
                .role("USER")
                .build();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        entityManager.persistAndFlush(testUser);

        Optional<User> result = userRepository.findByEmail("mohamed@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Mohamed Khalid");
        assertThat(result.get().getRole()).isEqualTo("USER");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailNotExists() {
        Optional<User> result = userRepository.findByEmail("notfound@gmail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_ShouldBeCaseInsensitive() {
        entityManager.persistAndFlush(testUser);

        Optional<User> result = userRepository.findByEmail("MOHAMED@GMAIL.COM");

        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistUser_WhenValidUser() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Mohamed Khalid");

        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("mohamed@gmail.com");
    }

    @Test
    void save_ShouldGenerateId_WhenUserHasNoId() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0L);
    }

    @Test
    void findAll_ShouldReturnAllUsers_WhenMultipleUsersExist() {
        User secondUser = User.builder()
                .fullName("Bhanu Priya")
                .email("priya@gmail.com")
                .password("priya")
                .role("ADMIN")
                .build();

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(secondUser);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("mohamed@gmail.com", "priya@gmail.com");
    }

    @Test
    void deleteById_ShouldRemoveUser_WhenNoUserExists() {
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush();

        User deletedUser = entityManager.find(User.class, userId);
        assertThat(deletedUser).isNull();
    }

    @Test
    void existsById_ShouldReturnTrue_WhenUserExists() {
        User savedUser = entityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsById(savedUser.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenUserNotExists() {
        boolean exists = userRepository.existsById(999L);
        assertThat(exists).isFalse();
    }

}
