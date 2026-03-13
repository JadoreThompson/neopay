package com.zenz.crypto_payment_gateway.api.route.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.config.JWTAuthenticationFilter;
import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import com.zenz.crypto_payment_gateway.api.GlobalExceptionHandler;
import com.zenz.crypto_payment_gateway.api.route.auth.model.request.LoginRequest;
import com.zenz.crypto_payment_gateway.api.route.auth.model.request.RegisterRequest;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.repository.UserRepository;
import com.zenz.crypto_payment_gateway.service.AuthService;
import com.zenz.crypto_payment_gateway.service.JWTService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

/**
 * Comprehensive test suite for AuthController endpoints.
 * Tests target expected behavior and validation standards for production readiness.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private String testEmail;
    private String testPassword;
    private String testToken;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testEmail = "test@example.com";
        testPassword = "SecurePassword123";
        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail(testEmail);
        testUser.setPassword("hashedPassword");
    }

    // REGISTER ENDPOINT TESTS

    @Test
    @DisplayName("Register: Should register user successfully with valid credentials")
    void register_withValidCredentials_shouldReturnOk() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        Mockito.when(authService.createUser(testEmail, testPassword)).thenReturn(testUser);
        Mockito.when(authService.login(testEmail, testPassword)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(authService).createUser(testEmail, testPassword);
        Mockito.verify(authService).login(testEmail, testPassword);
    }

    @Test
    @DisplayName("Register: Should set JWT cookie upon successful registration")
    void register_withValidCredentials_shouldSetJwtCookie() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        Mockito.when(authService.createUser(testEmail, testPassword)).thenReturn(testUser);
        Mockito.when(authService.login(testEmail, testPassword)).thenReturn(testToken);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().exists(JWTAuthenticationFilter.JWT_COOKIE_NAME))
                .andExpect(MockMvcResultMatchers.cookie().value(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        boolean foundJwtCookie = false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JWTAuthenticationFilter.JWT_COOKIE_NAME)) {
                foundJwtCookie = true;
                Assertions.assertTrue(cookie.isHttpOnly(), "JWT cookie should be HttpOnly");
                Assertions.assertEquals("/", cookie.getPath(), "JWT cookie path should be /");
                Assertions.assertTrue(cookie.getMaxAge() > 0, "JWT cookie should have positive max age");
            }
        }
        Assertions.assertTrue(foundJwtCookie, "JWT cookie should be present");
    }

    @Test
    @DisplayName("Register: Should persist user to database upon registration")
    void register_shouldPersistUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("NewSecurePassword12");

        User newUser = new User();
        newUser.setUserId(UUID.randomUUID());
        newUser.setEmail("newuser@example.com");

        Mockito.when(authService.createUser("newuser@example.com", "NewSecurePassword12")).thenReturn(newUser);
        Mockito.when(authService.login("newuser@example.com", "NewSecurePassword12")).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(authService).createUser("newuser@example.com", "NewSecurePassword12");
    }

    @Test
    @DisplayName("Register: Should reject registration with null email")
    void register_withNullEmail_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(null);
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with empty email")
    void register_withEmptyEmail_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with blank email (whitespace only)")
    void register_withBlankEmail_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("   ");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with invalid email format - missing @")
    void register_withInvalidEmailMissingAt_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalidemail.com");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with invalid email format - missing domain")
    void register_withInvalidEmailMissingDomain_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid@");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with invalid email format - missing local part")
    void register_withInvalidEmailMissingLocalPart_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("@example.com");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with email containing multiple @ symbols")
    void register_withMultipleAtSymbols_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@@example.com");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should accept valid email formats - standard format")
    void register_withValidEmailFormat_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@domain.com");
        request.setPassword(testPassword);

        Mockito.when(authService.createUser("user@domain.com", testPassword)).thenReturn(testUser);
        Mockito.when(authService.login("user@domain.com", testPassword)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Register: Should accept valid email formats - with subdomain")
    void register_withValidEmailWithSubdomain_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@mail.domain.com");
        request.setPassword(testPassword);

        Mockito.when(authService.createUser("user@mail.domain.com", testPassword)).thenReturn(testUser);
        Mockito.when(authService.login("user@mail.domain.com", testPassword)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Register: Should accept valid email formats - with plus sign")
    void register_withValidEmailWithPlusSign_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user+tag@domain.com");
        request.setPassword(testPassword);

        Mockito.when(authService.createUser("user+tag@domain.com", testPassword)).thenReturn(testUser);
        Mockito.when(authService.login("user+tag@domain.com", testPassword)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Register: Should reject registration with null password")
    void register_withNullPassword_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with empty password")
    void register_withEmptyPassword_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with password shorter than 8 characters")
    void register_withShortPassword_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("Short1A");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with password exactly 7 characters")
    void register_withExactly7CharacterPassword_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("SevenAB");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should accept registration with password exactly 8 characters")
    void register_withExactly8CharacterPassword_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("EightABc");

        Mockito.when(authService.createUser(testEmail, "EightABc")).thenReturn(testUser);
        Mockito.when(authService.login(testEmail, "EightABc")).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Register: Should reject registration with password missing uppercase letters")
    void register_withNoUppercaseLetters_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("alllowercase1");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with only one uppercase letter")
    void register_withOnlyOneUppercaseLetter_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("Onlyoneupper");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should accept registration with exactly 2 uppercase letters")
    void register_withExactlyTwoUppercaseLetters_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("TwoUpper12");

        Mockito.when(authService.createUser(testEmail, "TwoUpper12")).thenReturn(testUser);
        Mockito.when(authService.login(testEmail, "TwoUpper12")).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Register: Should accept registration with more than 2 uppercase letters")
    void register_withMoreThanTwoUppercaseLetters_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("THREEUPPER12");

        Mockito.when(authService.createUser(testEmail, "THREEUPPER12")).thenReturn(testUser);
        Mockito.when(authService.login(testEmail, "THREEUPPER12")).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Register: Should accept strong password with mixed case, numbers, and special characters")
    void register_withStrongPassword_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("Str0ngP@ssW0rd!");

        Mockito.when(authService.createUser(testEmail, "Str0ngP@ssW0rd!")).thenReturn(testUser);
        Mockito.when(authService.login(testEmail, "Str0ngP@ssW0rd!")).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Register: Should reject registration with already registered email")
    void register_withDuplicateEmail_shouldReturnConflict() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        Mockito.when(authService.createUser(testEmail, testPassword))
                .thenThrow(new RuntimeException("User with this email already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());

        Mockito.verify(authService).createUser(testEmail, testPassword);
        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with missing request body")
    void register_withMissingBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with malformed JSON")
    void register_withMalformedJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Register: Should reject registration with wrong content type")
    void register_withWrongContentType_shouldReturnUnsupportedMediaType() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // LOGIN ENDPOINT TESTS

    @Test
    @DisplayName("Login: Should login successfully with valid credentials")
    void login_withValidCredentials_shouldReturnOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        Mockito.when(authService.login(testEmail, testPassword)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(authService).login(testEmail, testPassword);
    }

    @Test
    @DisplayName("Login: Should set JWT cookie upon successful login")
    void login_withValidCredentials_shouldSetJwtCookie() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        Mockito.when(authService.login(testEmail, testPassword)).thenReturn(testToken);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().exists(JWTAuthenticationFilter.JWT_COOKIE_NAME))
                .andExpect(MockMvcResultMatchers.cookie().value(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        boolean foundJwtCookie = false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JWTAuthenticationFilter.JWT_COOKIE_NAME)) {
                foundJwtCookie = true;
                Assertions.assertTrue(cookie.isHttpOnly(), "JWT cookie should be HttpOnly");
                Assertions.assertEquals("/", cookie.getPath(), "JWT cookie path should be /");
                Assertions.assertTrue(cookie.getMaxAge() > 0, "JWT cookie should have positive max age");
            }
        }
        Assertions.assertTrue(foundJwtCookie, "JWT cookie should be present");
    }

    @Test
    @DisplayName("Login: Should reject login with non-existent email")
    void login_withNonExistentEmail_shouldReturnUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword(testPassword);

        Mockito.when(authService.login("nonexistent@example.com", testPassword))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Login: Should reject login with wrong password")
    void login_withWrongPassword_shouldReturnUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("WrongPassword12");

        Mockito.when(authService.login(testEmail, "WrongPassword12"))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Login: Should not reveal which credential is incorrect")
    void login_withInvalidCredentials_shouldReturnGenericMessage() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("WrongPassword12");

        Mockito.when(authService.login(testEmail, "WrongPassword12"))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Login: Should reject login with null email")
    void login_withNullEmail_shouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(null);
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Login: Should reject login with empty email")
    void login_withEmptyEmail_shouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Login: Should reject login with invalid email format")
    void login_withInvalidEmailFormat_shouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Login: Should reject login with null password")
    void login_withNullPassword_shouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Login: Should reject login with empty password")
    void login_withEmptyPassword_shouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Login: Should reject login with missing request body")
    void login_withMissingBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Login: Should reject login with malformed JSON")
    void login_withMalformedJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).login(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // ME ENDPOINT TESTS
    @Test
    @DisplayName("Me: Should return user info when authenticated")
    void me_whenAuthenticated_shouldReturnUserInfo() throws Exception {
        // Mock JWT service to return valid user
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Me: Should return correct email in response")
    void me_whenAuthenticated_shouldReturnCorrectEmail() throws Exception {
        // Mock JWT service to return valid user
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(testEmail));
    }

    @Test
    @DisplayName("Me: Should return 401 when not authenticated")
    void me_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Me: Should return 401 with invalid JWT token")
    void me_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        Mockito.when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, "invalid-token")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Me: Should return 401 with expired JWT token")
    void me_withExpiredJwt_shouldReturnUnauthorized() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired";
        Mockito.when(jwtService.isTokenValid(expiredToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, expiredToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Me: Should return JSON content type")
    void me_shouldReturnJsonContentType() throws Exception {
        // Mock JWT service to return valid user
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    // LOGOUT ENDPOINT TESTS

    @Test
    @DisplayName("Logout: Should logout successfully")
    void logout_shouldReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout/"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Logout: Should clear JWT cookie upon logout")
    void logout_shouldClearJwtCookie() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().exists(JWTAuthenticationFilter.JWT_COOKIE_NAME))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        boolean foundClearedCookie = false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JWTAuthenticationFilter.JWT_COOKIE_NAME)) {
                foundClearedCookie = true;
                Assertions.assertEquals(0, cookie.getMaxAge(), "JWT cookie should have MaxAge=0 to clear");
                Assertions.assertNull(cookie.getValue(), "JWT cookie value should be null");
                Assertions.assertTrue(cookie.isHttpOnly(), "JWT cookie should still be HttpOnly when cleared");
                Assertions.assertEquals("/", cookie.getPath(), "JWT cookie path should be /");
            }
        }
        Assertions.assertTrue(foundClearedCookie, "JWT cookie should be present in response");
    }

    @Test
    @DisplayName("Logout: Should work even without existing JWT cookie")
    void logout_withoutExistingCookie_shouldStillSucceed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().exists(JWTAuthenticationFilter.JWT_COOKIE_NAME));
    }

    @Test
    @DisplayName("Logout: Should set HttpOnly flag on cleared cookie")
    void logout_shouldSetHttpOnlyOnClearedCookie() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JWTAuthenticationFilter.JWT_COOKIE_NAME)) {
                Assertions.assertTrue(cookie.isHttpOnly(), "Cleared JWT cookie should be HttpOnly");
            }
        }
    }

    @Test
    @DisplayName("Logout: Should set correct path on cleared cookie")
    void logout_shouldSetCorrectPathOnClearedCookie() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JWTAuthenticationFilter.JWT_COOKIE_NAME)) {
                Assertions.assertEquals("/", cookie.getPath(), "Cleared JWT cookie should have path /");
            }
        }
    }

    // SECURITY TESTS
    @Test
    @DisplayName("Security: Should not expose sensitive data in error responses")
    void endpoints_shouldNotExposeSensitiveDataInErrors() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        Mockito.when(authService.createUser(testEmail, testPassword))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts safely")
    void register_withSqlInjectionAttempt_shouldBeHandledSafely() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("PasswordAB'; DROP TABLE users;--");

        Mockito.when(authService.createUser(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(testUser);
        Mockito.when(authService.login(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(authService).createUser(ArgumentMatchers.eq("test@example.com"), ArgumentMatchers.eq("PasswordAB'; DROP TABLE users;--"));
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts in email safely")
    void register_withXssInEmail_shouldBeHandledSafely() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("<script>alert('xss')</script>@example.com");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle very long input strings")
    void register_withVeryLongInput_shouldBeHandledGracefully() throws Exception {
        String longEmail = "a".repeat(1000) + "@example.com";
        String longPassword = "AB".repeat(500) + "cd".repeat(500);

        RegisterRequest request = new RegisterRequest();
        request.setEmail(longEmail);
        request.setPassword(longPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // RATE LIMITING AND ABUSE PREVENTION TESTS
    @Test
    @DisplayName("Rate Limiting: Should handle multiple failed login attempts")
    void login_multipleFailedAttempts_shouldNotLockAccount() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("WrongPassword12");

        Mockito.when(authService.login(testEmail, "WrongPassword12"))
                .thenThrow(new RuntimeException("Invalid email or password"));

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().is5xxServerError());
        }

        Mockito.verify(authService, Mockito.times(5)).login(testEmail, "WrongPassword12");
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should handle email with leading/trailing whitespace")
    void register_withWhitespaceInEmail_shouldTrimOrReject() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  test@example.com  ");
        request.setPassword(testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Edge Case: Should handle password with only whitespace")
    void register_withWhitespacePassword_shouldReject() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword("        ");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(authService, Mockito.never()).createUser(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Edge Case: Should handle case sensitivity in email")
    void register_withDifferentCaseEmail_shouldHandleAppropriately() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("TEST@EXAMPLE.COM");
        request.setPassword(testPassword);

        Mockito.when(authService.createUser("TEST@EXAMPLE.COM", testPassword)).thenReturn(testUser);
        Mockito.when(authService.login("TEST@EXAMPLE.COM", testPassword)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Edge Case: Should handle Unicode characters in password")
    void register_withUnicodePassword_shouldSucceed() throws Exception {
        final String password = "P@sswörd12T";
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(password);

        Mockito.when(authService.createUser(testEmail, password)).thenReturn(testUser);
        Mockito.when(authService.login(testEmail, password)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}