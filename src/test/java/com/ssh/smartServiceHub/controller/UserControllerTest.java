package com.ssh.smartServiceHub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssh.smartServiceHub.dto.UserDTO;
import com.ssh.smartServiceHub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUserDTO;
    private List<UserDTO> userList;

    @BeforeEach
    void setup() {
        testUserDTO = UserDTO.builder()
                .id(1L)
                .fullName("Mohamed Khalid")
                .email("mohamed@gmail.com")
                .role("USER")
                .build();

        UserDTO adminUserDTO = UserDTO.builder()
                .id(2L)
                .fullName("Bhanu Priya")
                .email("priya@gmail.com")
                .role("ADMIN")
                .build();

        userList = Arrays.asList(testUserDTO, adminUserDTO);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnListOfUsers_WhenAuthenticated() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fullName", is("Mohamed Khalid")))
                .andExpect(jsonPath("$[0].email", is("mohamed@gmail.com")))
                .andExpect(jsonPath("$[0].role", is("USER")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fullName", is("Bhanu Priya")));

        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1L)))
                .andExpect(jsonPath("$.fullName", is("Mohamed Khalid")))
                .andExpect(jsonPath("$.email", is("mohamed@gmail.com")));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createNewUser_ShouldReturnCreatedUser_WhenValidUser() throws Exception {
        UserDTO newUserDTO = UserDTO.builder()
                .fullName("Bariki Ambika")
                .email("ambika@gmail.com")
                .password("ambika")
                .role("USER")
                .build();

        UserDTO createdUserDTO = UserDTO.builder()
                .id(3L)
                .fullName("Bariki Ambika")
                .email("ambika@gmail.com")
                .role("USER")
                .build();

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUserDTO);

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3L)))
                .andExpect(jsonPath("$.fullName", is("Bariki Ambika")))
                .andExpect(jsonPath("$.email", is("ambika@gmail.com")));

        verify(userService).createUser(any(UserDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateExistingUser_ShouldReturnUpdatedUser_WhenValidUpdate() throws Exception {
        UserDTO userUpdateDTO = UserDTO.builder()
                .fullName("Mohamed Khalid")
                .email("mohamed@gmail.com")
                .password("mohamed")
                .role("USER")
                .build();

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(1L)
                .fullName("Mohamed Khalid")
                .email("mohamed@gmail.com")
                .role("ADMIN")
                .build();

        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(updatedUserDTO);

        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1L)))
                .andExpect(jsonPath("$.fullName", is("Mohamed Khalid")))
                .andExpect(jsonPath("$.email", is("mohamed@gmail.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")));

        verify(userService).updateUser(eq(1L), any(UserDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturnNoContent_WhenUserExists() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createNewUser_ShouldReturnBadRequest_WhenServiceThrowsException() throws Exception {
        UserDTO newUserDTO = UserDTO.builder()
                .fullName("Test User")
                .email("mohamed@gmail.com")
                .password("mohamed")
                .role("USER")
                .build();

        when(userService.createUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Email already exists")));
    }
}
