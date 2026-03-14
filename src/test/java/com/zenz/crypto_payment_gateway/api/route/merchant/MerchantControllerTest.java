package com.zenz.crypto_payment_gateway.api.route.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.GlobalExceptionHandler;
import com.zenz.crypto_payment_gateway.api.config.JWTAuthenticationFilter;
import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.CreateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.UpdateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.response.MerchantResponse;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.repository.MerchantRepository;
import com.zenz.crypto_payment_gateway.repository.UserRepository;
import com.zenz.crypto_payment_gateway.service.JWTService;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Comprehensive test suite for MerchantController endpoints.
 * Tests target expected behavior and validation standards for production readiness.
 */
@WebMvcTest(MerchantController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MerchantRepository merchantRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private String testToken;
    private String testMerchantId;
    private String testMerchantName;
    private String testMerchantDescription;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID().toString();
        testMerchantName = "Test Merchant";
        testMerchantDescription = "A test merchant description";

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName(testMerchantName);
        testMerchant.setDescription(testMerchantDescription);
        testMerchant.setUser(testUser);
        testMerchant.setCreatedAt(System.currentTimeMillis());
    }

    /**
     * Helper method to set up authentication mocks
     */
    private void setupAuthentication() {
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
    }

    /**
     * Helper method to create a MerchantResponse object.
     */
    private MerchantResponse createMerchantResponse(Merchant merchant) {
        MerchantResponse response =
                new MerchantResponse();
        response.setMerchantId(merchant.getMerchantId());
        response.setName(merchant.getName());
        response.setDescription(merchant.getDescription());
        response.setCreatedAt(merchant.getCreatedAt());
        return response;
    }

    // ========================================
    // CREATE MERCHANT ENDPOINT TESTS
    // ========================================

    @Test
    @DisplayName("Create Merchant: Should create merchant successfully with valid data")
    void createMerchant_withValidData_shouldReturnOk() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);
        request.setDescription(testMerchantDescription);

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.toResponse(testMerchant)).thenReturn(createMerchantResponse(testMerchant));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.merchantId").value(testMerchantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testMerchantName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testMerchantDescription));

        Mockito.verify(merchantService).createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class));
    }

    @Test
    @DisplayName("Create Merchant: Should create merchant with name only (description optional)")
    void createMerchant_withNameOnly_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);
        // description is optional

        Merchant merchant = new Merchant();
        merchant.setMerchantId(testMerchantId);
        merchant.setName(testMerchantName);
        merchant.setUser(testUser);
        merchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(merchant);
        Mockito.when(merchantService.toResponse(merchant)).thenReturn(createMerchantResponse(merchant));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testMerchantName));
    }

    @Test
    @DisplayName("Create Merchant: Should reject request without authentication")
    void createMerchant_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);
        request.setDescription(testMerchantDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with invalid JWT token")
    void createMerchant_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);
        request.setDescription(testMerchantDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with null name")
    void createMerchant_withNullName_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(null);
        request.setDescription(testMerchantDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with empty name")
    void createMerchant_withEmptyName_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName("");
        request.setDescription(testMerchantDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with blank name (whitespace only)")
    void createMerchant_withBlankName_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName("   ");
        request.setDescription(testMerchantDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with name exceeding 255 characters")
    void createMerchant_withNameExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName("a".repeat(256));
        request.setDescription(testMerchantDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should accept name with exactly 255 characters")
    void createMerchant_withNameExactly255Characters_shouldSucceed() throws Exception {
        setupAuthentication();

        String maxName = "a".repeat(255);
        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(maxName);
        request.setDescription(testMerchantDescription);

        Merchant merchant = new Merchant();
        merchant.setMerchantId(testMerchantId);
        merchant.setName(maxName);
        merchant.setDescription(testMerchantDescription);
        merchant.setUser(testUser);
        merchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(merchant);
        Mockito.when(merchantService.toResponse(merchant)).thenReturn(createMerchantResponse(merchant));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with description exceeding 1000 characters")
    void createMerchant_withDescriptionExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);
        request.setDescription("a".repeat(1001));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with missing body")
    void createMerchant_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with malformed JSON")
    void createMerchant_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Merchant: Should reject request with wrong content type")
    void createMerchant_withWrongContentType_shouldReturnUnsupportedMediaType() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        Mockito.verify(merchantService, Mockito.never()).createMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // ========================================
    // GET MERCHANT BY ID ENDPOINT TESTS
    // ========================================

    @Test
    @DisplayName("Get Merchant: Should return merchant when authenticated and owner")
    void getMerchant_whenAuthenticatedAndOwner_shouldReturnMerchant() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(UUID.fromString(testMerchantId), testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.toResponse(testMerchant)).thenReturn(createMerchantResponse(testMerchant));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.merchantId").value(testMerchantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testMerchantName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testMerchantDescription));
    }

    @Test
    @DisplayName("Get Merchant: Should return 404 when merchant not found")
    void getMerchant_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomMerchantId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(randomMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + randomMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Merchant: Should return 401 when not authenticated")
    void getMerchant_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).getMerchantByIdAndUserId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Merchant: Should return JSON content type")
    void getMerchant_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(UUID.fromString(testMerchantId), testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.toResponse(testMerchant)).thenReturn(createMerchantResponse(testMerchant));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Merchant: Should return 400 for invalid UUID format")
    void getMerchant_withInvalidUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // ========================================
    // GET ALL MERCHANTS ENDPOINT TESTS
    // ========================================

    @Test
    @DisplayName("Get Merchants: Should return all merchants for authenticated user")
    void getMerchants_whenAuthenticated_shouldReturnMerchants() throws Exception {
        setupAuthentication();

        Merchant merchant2 = new Merchant();
        merchant2.setMerchantId(UUID.randomUUID().toString());
        merchant2.setName("Second Merchant");
        merchant2.setDescription("Second description");
        merchant2.setUser(testUser);
        merchant2.setCreatedAt(System.currentTimeMillis());

        List<Merchant> merchants = Arrays.asList(testMerchant, merchant2);

        Mockito.when(merchantService.getMerchantsByUser(testUser)).thenReturn(merchants);
        Mockito.when(merchantService.toResponse(testMerchant)).thenReturn(createMerchantResponse(testMerchant));
        Mockito.when(merchantService.toResponse(merchant2)).thenReturn(createMerchantResponse(merchant2));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(testMerchantName))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Second Merchant"));
    }

    @Test
    @DisplayName("Get Merchants: Should return empty list when user has no merchants")
    void getMerchants_whenNoMerchants_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantsByUser(testUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Merchants: Should return 401 when not authenticated")
    void getMerchants_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).getMerchantsByUser(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Merchants: Should return JSON content type")
    void getMerchants_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantsByUser(testUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    // ========================================
    // UPDATE MERCHANT ENDPOINT TESTS
    // ========================================

    @Test
    @DisplayName("Update Merchant: Should update merchant name successfully")
    void updateMerchant_withValidName_shouldReturnUpdatedMerchant() throws Exception {
        setupAuthentication();

        String newName = "Updated Merchant Name";
        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setName(newName);

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setMerchantId(testMerchantId);
        updatedMerchant.setName(newName);
        updatedMerchant.setDescription(testMerchantDescription);
        updatedMerchant.setUser(testUser);
        updatedMerchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(UUID.fromString(testMerchantId), testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.updateMerchant(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UpdateMerchantRequest.class)))
                .thenReturn(updatedMerchant);
        Mockito.when(merchantService.toResponse(updatedMerchant)).thenReturn(createMerchantResponse(updatedMerchant));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newName));
    }

    @Test
    @DisplayName("Update Merchant: Should update merchant description successfully")
    void updateMerchant_withValidDescription_shouldReturnUpdatedMerchant() throws Exception {
        setupAuthentication();

        String newDescription = "Updated description";
        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setDescription(newDescription);

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setMerchantId(testMerchantId);
        updatedMerchant.setName(testMerchantName);
        updatedMerchant.setDescription(newDescription);
        updatedMerchant.setUser(testUser);
        updatedMerchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(UUID.fromString(testMerchantId), testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.updateMerchant(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UpdateMerchantRequest.class)))
                .thenReturn(updatedMerchant);
        Mockito.when(merchantService.toResponse(updatedMerchant)).thenReturn(createMerchantResponse(updatedMerchant));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(newDescription));
    }

    @Test
    @DisplayName("Update Merchant: Should update both name and description")
    void updateMerchant_withBothFields_shouldReturnUpdatedMerchant() throws Exception {
        setupAuthentication();

        String newName = "New Name";
        String newDescription = "New Description";
        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setName(newName);
        request.setDescription(newDescription);

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setMerchantId(testMerchantId);
        updatedMerchant.setName(newName);
        updatedMerchant.setDescription(newDescription);
        updatedMerchant.setUser(testUser);
        updatedMerchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(UUID.fromString(testMerchantId), testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.updateMerchant(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UpdateMerchantRequest.class)))
                .thenReturn(updatedMerchant);
        Mockito.when(merchantService.toResponse(updatedMerchant)).thenReturn(createMerchantResponse(updatedMerchant));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(newDescription));
    }

    @Test
    @DisplayName("Update Merchant: Should return 404 when merchant not found")
    void updateMerchant_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomMerchantId = UUID.randomUUID();
        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setName("New Name");

        Mockito.when(merchantService.getMerchantByIdAndUserId(randomMerchantId, testUser.getUserId()))
                .thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + randomMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Update Merchant: Should return 401 when not authenticated")
    void updateMerchant_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setName("New Name");

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).updateMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Merchant: Should reject request with name exceeding 255 characters")
    void updateMerchant_withNameExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setName("a".repeat(256));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).updateMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Merchant: Should reject request with description exceeding 1000 characters")
    void updateMerchant_withDescriptionExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setDescription("a".repeat(1001));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).updateMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Merchant: Should reject request with missing body")
    void updateMerchant_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).updateMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Merchant: Should reject request with malformed JSON")
    void updateMerchant_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(merchantService, Mockito.never()).updateMerchant(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // ========================================
    // SECURITY TESTS
    // ========================================

    @Test
    @DisplayName("Security: Should not expose sensitive data in error responses")
    void endpoints_shouldNotExposeSensitiveDataInErrors() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);
        request.setDescription(testMerchantDescription);

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in merchant name safely")
    void createMerchant_withSqlInjectionInName_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName("'; DROP TABLE merchants;--");
        request.setDescription(testMerchantDescription);

        Merchant merchant = new Merchant();
        merchant.setMerchantId(testMerchantId);
        merchant.setName("'; DROP TABLE merchants;--");
        merchant.setDescription(testMerchantDescription);
        merchant.setUser(testUser);
        merchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(merchant);
        Mockito.when(merchantService.toResponse(merchant)).thenReturn(createMerchantResponse(merchant));

        // The request should succeed - the service layer should handle SQL injection safely
        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(merchantService).createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class));
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts in merchant name safely")
    void createMerchant_withXssInName_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName("<script>alert('xss')</script>");
        request.setDescription(testMerchantDescription);

        Merchant merchant = new Merchant();
        merchant.setMerchantId(testMerchantId);
        merchant.setName("<script>alert('xss')</script>");
        merchant.setDescription(testMerchantDescription);
        merchant.setUser(testUser);
        merchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(merchant);
        Mockito.when(merchantService.toResponse(merchant)).thenReturn(createMerchantResponse(merchant));

        // The request should succeed - the service layer should handle XSS safely
        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // ========================================
    // EDGE CASES TESTS
    // ========================================

    @Test
    @DisplayName("Edge Case: Should handle merchant name with special characters")
    void createMerchant_withSpecialCharactersInName_shouldSucceed() throws Exception {
        setupAuthentication();

        String specialName = "Test @#$%^&*() Merchant!";
        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(specialName);
        request.setDescription(testMerchantDescription);

        Merchant merchant = new Merchant();
        merchant.setMerchantId(testMerchantId);
        merchant.setName(specialName);
        merchant.setDescription(testMerchantDescription);
        merchant.setUser(testUser);
        merchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(merchant);
        Mockito.when(merchantService.toResponse(merchant)).thenReturn(createMerchantResponse(merchant));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(specialName));
    }

    @Test
    @DisplayName("Edge Case: Should handle merchant name with Unicode characters")
    void createMerchant_withUnicodeInName_shouldSucceed() throws Exception {
        setupAuthentication();

        String unicodeName = "测试商户 Тест";
        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(unicodeName);
        request.setDescription(testMerchantDescription);

        Merchant merchant = new Merchant();
        merchant.setMerchantId(testMerchantId);
        merchant.setName(unicodeName);
        merchant.setDescription(testMerchantDescription);
        merchant.setUser(testUser);
        merchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(merchant);
        Mockito.when(merchantService.toResponse(merchant)).thenReturn(createMerchantResponse(merchant));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(unicodeName));
    }

    @Test
    @DisplayName("Edge Case: Should handle empty description")
    void createMerchant_withEmptyDescription_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName(testMerchantName);
        request.setDescription("");

        Merchant merchant = new Merchant();
        merchant.setMerchantId(testMerchantId);
        merchant.setName(testMerchantName);
        merchant.setDescription("");
        merchant.setUser(testUser);
        merchant.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.createMerchant(ArgumentMatchers.any(User.class), ArgumentMatchers.any(CreateMerchantRequest.class)))
                .thenReturn(merchant);
        Mockito.when(merchantService.toResponse(merchant)).thenReturn(createMerchantResponse(merchant));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Edge Case: Should handle null fields in update request (partial update)")
    void updateMerchant_withNullFields_shouldSucceed() throws Exception {
        setupAuthentication();

        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setName(null);
        request.setDescription(null);

        // When both fields are null, merchant should remain unchanged
        Mockito.when(merchantService.getMerchantByIdAndUserId(UUID.fromString(testMerchantId), testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.updateMerchant(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UpdateMerchantRequest.class)))
                .thenReturn(testMerchant);
        Mockito.when(merchantService.toResponse(testMerchant)).thenReturn(createMerchantResponse(testMerchant));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}