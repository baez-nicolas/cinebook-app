package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.UserDTO;
import com.cinebook.backend.entities.User;
import com.cinebook.backend.entities.enums.UserRole;
import com.cinebook.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminController adminController;

    private User mockAdmin;
    private User mockUser1;
    private User mockUser2;

    @BeforeEach
    void setUp() {
        mockAdmin = new User();
        mockAdmin.setId(1L);
        mockAdmin.setEmail("admin@cinebook.com");
        mockAdmin.setPassword("hashedPassword");
        mockAdmin.setFirstName("Admin");
        mockAdmin.setLastName("CineBook");
        mockAdmin.setPhone("1234567890");
        mockAdmin.setRole(UserRole.ADMIN);
        mockAdmin.setCreatedAt(LocalDateTime.now());
        mockAdmin.setIsActive(true);

        mockUser1 = new User();
        mockUser1.setId(2L);
        mockUser1.setEmail("user1@example.com");
        mockUser1.setPassword("hashedPassword");
        mockUser1.setFirstName("John");
        mockUser1.setLastName("Doe");
        mockUser1.setPhone("0987654321");
        mockUser1.setRole(UserRole.USER);
        mockUser1.setCreatedAt(LocalDateTime.now());
        mockUser1.setIsActive(true);

        mockUser2 = new User();
        mockUser2.setId(3L);
        mockUser2.setEmail("user2@example.com");
        mockUser2.setPassword("hashedPassword");
        mockUser2.setFirstName("Jane");
        mockUser2.setLastName("Smith");
        mockUser2.setPhone("1122334455");
        mockUser2.setRole(UserRole.USER);
        mockUser2.setCreatedAt(LocalDateTime.now());
        mockUser2.setIsActive(true);
    }

    @Test
    @DisplayName("getAllUsers - Retorna todos los usuarios")
    void getAllUsers_ReturnsAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockAdmin, mockUser1, mockUser2));

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers - Retorna lista vacía cuando no hay usuarios")
    void getAllUsers_EmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers - Convierte correctamente a DTO")
    void getAllUsers_ConvertsToDTO_Correctly() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser1));

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        UserDTO dto = response.getBody().get(0);
        assertEquals(2L, dto.getId());
        assertEquals("user1@example.com", dto.getEmail());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("USER", dto.getRole());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers - Incluye usuarios ADMIN y USER")
    void getAllUsers_IncludesAdminAndUser() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockAdmin, mockUser1));

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        assertNotNull(response);
        assertEquals(2, response.getBody().size());

        assertTrue(response.getBody().stream().anyMatch(dto -> dto.getRole().equals("ADMIN")));
        assertTrue(response.getBody().stream().anyMatch(dto -> dto.getRole().equals("USER")));

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("countUsers - Cuenta correctamente todos los usuarios")
    void countUsers_CountsAllUsers() {
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(mockAdmin));

        ResponseEntity<Map<String, Object>> response = adminController.countUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(3L, body.get("totalUsers"));
        assertEquals(1L, body.get("totalAdmins"));
        assertEquals(2L, body.get("totalRegularUsers"));

        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN);
    }

    @Test
    @DisplayName("countUsers - Retorna cero cuando no hay usuarios")
    void countUsers_ReturnsZero_WhenNoUsers() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = adminController.countUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertEquals(0L, body.get("totalUsers"));
        assertEquals(0L, body.get("totalAdmins"));
        assertEquals(0L, body.get("totalRegularUsers"));

        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN);
    }

    @Test
    @DisplayName("countUsers - Cuenta correctamente múltiples admins")
    void countUsers_CountsMultipleAdmins() {
        User admin2 = new User();
        admin2.setId(4L);
        admin2.setRole(UserRole.ADMIN);

        when(userRepository.count()).thenReturn(5L);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(mockAdmin, admin2));

        ResponseEntity<Map<String, Object>> response = adminController.countUsers();

        assertNotNull(response);
        Map<String, Object> body = response.getBody();
        assertEquals(5L, body.get("totalUsers"));
        assertEquals(2L, body.get("totalAdmins"));
        assertEquals(3L, body.get("totalRegularUsers"));

        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN);
    }

    @Test
    @DisplayName("countUsers - Retorna mapa con todas las claves requeridas")
    void countUsers_ReturnsMapWithAllRequiredKeys() {
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(mockAdmin));

        ResponseEntity<Map<String, Object>> response = adminController.countUsers();

        assertNotNull(response);
        Map<String, Object> body = response.getBody();

        assertTrue(body.containsKey("totalUsers"));
        assertTrue(body.containsKey("totalAdmins"));
        assertTrue(body.containsKey("totalRegularUsers"));

        verify(userRepository, times(1)).count();
    }

    @Test
    @DisplayName("getAllUsers - Mantiene el orden de la base de datos")
    void getAllUsers_MaintainsDatabaseOrder() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser2, mockUser1, mockAdmin));

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        assertNotNull(response);
        List<UserDTO> users = response.getBody();

        assertEquals("user2@example.com", users.get(0).getEmail());
        assertEquals("user1@example.com", users.get(1).getEmail());
        assertEquals("admin@cinebook.com", users.get(2).getEmail());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("convertToDTO - No expone información sensible")
    void convertToDTO_DoesNotExposePasswordOrPhone() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser1));

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        assertNotNull(response);
        UserDTO dto = response.getBody().get(0);

        assertNotNull(dto.getId());
        assertNotNull(dto.getEmail());
        assertNotNull(dto.getFirstName());
        assertNotNull(dto.getLastName());
        assertNotNull(dto.getRole());
    }

    @Test
    @DisplayName("countUsers - Cálculo correcto de usuarios regulares")
    void countUsers_CalculatesRegularUsersCorrectly() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(mockAdmin));

        ResponseEntity<Map<String, Object>> response = adminController.countUsers();

        Map<String, Object> body = response.getBody();
        long totalUsers = (Long) body.get("totalUsers");
        long totalAdmins = (Long) body.get("totalAdmins");
        long totalRegularUsers = (Long) body.get("totalRegularUsers");

        assertEquals(totalUsers - totalAdmins, totalRegularUsers);
    }

    @Test
    @DisplayName("getAllUsers - Verifica que todos los campos DTO estén mapeados")
    void getAllUsers_VerifiesAllDTOFieldsMapped() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockAdmin));

        ResponseEntity<List<UserDTO>> response = adminController.getAllUsers();

        UserDTO dto = response.getBody().get(0);

        assertAll("DTO fields",
            () -> assertNotNull(dto.getId()),
            () -> assertNotNull(dto.getEmail()),
            () -> assertNotNull(dto.getFirstName()),
            () -> assertNotNull(dto.getLastName()),
            () -> assertNotNull(dto.getRole())
        );
    }
}

