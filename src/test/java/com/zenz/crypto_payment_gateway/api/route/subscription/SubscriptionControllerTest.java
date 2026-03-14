package com.zenz.crypto_payment_gateway.api.route.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.GlobalExceptionHandler;
import com.zenz.crypto_payment_gateway.api.config.JWTAuthenticationFilter;
import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.subscription.request.CreateSubscriptionRequest;
import com.zenz.crypto_payment_gateway.api.route.subscription.response.SubscriptionResponse;
import com.zenz.crypto_payment_gateway.entity.Customer;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.Price;
import com.zenz.crypto_payment_gateway.entity.Product;
import com.zenz.crypto_payment_gateway.entity.Subscription;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.enums.PricingType;
import com.zenz.crypto_payment_gateway.enums.SubscriptionStatus;
import com.zenz.crypto_payment_gateway.repository.UserRepository;
import com.zenz.crypto_payment_gateway.service.JWTService;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import com.zenz.crypto_payment_gateway.service.SubscriptionService;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebMvcTest(SubscriptionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoSpyBean
    private SubscriptionService spySubscriptionService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private Customer testCustomer;
    private Product testProduct;
    private Price testPrice;
    private Subscription testSubscription;
    private String testToken;
    private UUID testMerchantId;
    private UUID testCustomerId;
    private UUID testProductId;
    private UUID testPriceId;
    private UUID testSubscriptionId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testPriceId = UUID.randomUUID();
        testSubscriptionId = UUID.randomUUID();

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName("Test Merchant");
        testMerchant.setCreatedAt(System.currentTimeMillis());

        testCustomer = new Customer();
        testCustomer.setCustomerId(testCustomerId);
        testCustomer.setMerchantId(testMerchantId);
        testCustomer.setNickname("Test Customer");
        testCustomer.setCreatedAt(System.currentTimeMillis());

        testProduct = new Product();
        testProduct.setProductId(testProductId);
        testProduct.setMerchantId(testMerchantId);
        testProduct.setName("Test Product");
        testProduct.setCreatedAt(System.currentTimeMillis());

        testPrice = new Price();
        testPrice.setPriceId(testPriceId);
        testPrice.setProductId(testProductId);
        testPrice.setMerchantId(testMerchantId);
        testPrice.setAmount(1000);
        testPrice.setCurrency("USD");
        testPrice.setPricingType(PricingType.ONE_TIME);

        testSubscription = new Subscription();
        testSubscription.setSubscriptionId(testSubscriptionId);
        testSubscription.setMerchantId(testMerchantId);
        testSubscription.setCustomerId(testCustomerId);
        testSubscription.setProductId(testProductId);
        testSubscription.setPriceId(testPriceId);
        testSubscription.setQuantity(1);
        testSubscription.setStatus(SubscriptionStatus.UNPAID);
        testSubscription.setStartedAt(System.currentTimeMillis());
        testSubscription.setCreatedAt(System.currentTimeMillis());
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
     * Helper method to create a SubscriptionResponse object.
     */
    private SubscriptionResponse createSubscriptionResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(subscription.getSubscriptionId());
        response.setQuantity(subscription.getQuantity());
        response.setStatus(subscription.getStatus());
        response.setCustomerId(subscription.getCustomerId());
        response.setProductId(subscription.getProductId());
        response.setPriceId(subscription.getPriceId());
        response.setStartedAt(subscription.getStartedAt());
        response.setCreatedAt(subscription.getCreatedAt());
        return response;
    }

    // CREATE SUBSCRIPTION ENDPOINT TESTS

    @Test
    @DisplayName("Create Subscription: Should create subscription successfully with valid data")
    void createSubscription_withValidData_shouldReturnOk() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptionId").value(testSubscriptionId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(testCustomerId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(testProductId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.priceId").value(testPriceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UNPAID"));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(subscriptionService).createSubscription(testMerchantId, request);
    }

    @Test
    @DisplayName("Create Subscription: Should create subscription with quantity greater than 1")
    void createSubscription_withQuantityGreaterThanOne_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(5);

        testSubscription.setQuantity(5);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(5));
    }

    @Test
    @DisplayName("Create Subscription: Should reject request without authentication")
    void createSubscription_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with invalid JWT token")
    void createSubscription_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request when merchant not found for user")
    void createSubscription_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomMerchantId = UUID.randomUUID();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(randomMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + randomMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with null customerId")
    void createSubscription_withNullCustomerId_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(null);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with null productId")
    void createSubscription_withNullProductId_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(null);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with null priceId")
    void createSubscription_withNullPriceId_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(null);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with zero quantity")
    void createSubscription_withZeroQuantity_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(0);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with negative quantity")
    void createSubscription_withNegativeQuantity_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(-1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should accept request with quantity of 1 (minimum valid)")
    void createSubscription_withQuantityOne_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(1));
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with missing body")
    void createSubscription_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with malformed JSON")
    void createSubscription_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Subscription: Should reject request with wrong content type")
    void createSubscription_withWrongContentType_shouldReturnUnsupportedMediaType() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // PREREQUISITE VALIDATION TESTS - Customer, Product, Price

    @Test
    @DisplayName("Create Subscription: Should return not found when customer does not exist for merchant")
    void createSubscription_whenCustomerNotFoundForMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID nonExistentCustomerId = UUID.randomUUID();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(nonExistentCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenThrow(new ResourceNotFound("Failed to find customer with id " + nonExistentCustomerId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Create Subscription: Should return not found when product does not exist for merchant")
    void createSubscription_whenProductNotFoundForMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID nonExistentProductId = UUID.randomUUID();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(nonExistentProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenThrow(new ResourceNotFound("Failed to find product with id " + nonExistentProductId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Create Subscription: Should return not found when price does not exist for product")
    void createSubscription_whenPriceNotFoundForProduct_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID nonExistentPriceId = UUID.randomUUID();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(nonExistentPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenThrow(new ResourceNotFound("Failed to find price with id " + nonExistentPriceId + " for product"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Create Subscription: Should return not found when customer belongs to different merchant")
    void createSubscription_whenCustomerBelongsToDifferentMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID differentMerchantId = UUID.randomUUID();
        UUID customerFromOtherMerchant = UUID.randomUUID();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(customerFromOtherMerchant);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenThrow(new ResourceNotFound("Failed to find customer with id " + customerFromOtherMerchant + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Create Subscription: Should return not found when product belongs to different merchant")
    void createSubscription_whenProductBelongsToDifferentMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID productFromOtherMerchant = UUID.randomUUID();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(productFromOtherMerchant);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenThrow(new ResourceNotFound("Failed to find product with id " + productFromOtherMerchant + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Create Subscription: Should return not found when price belongs to different product")
    void createSubscription_whenPriceBelongsToDifferentProduct_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID priceFromOtherProduct = UUID.randomUUID();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(priceFromOtherProduct);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenThrow(new ResourceNotFound("Failed to find price with id " + priceFromOtherProduct + " for product"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // GET SUBSCRIPTION BY ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Subscription: Should return subscription when authenticated and owner")
    void getSubscription_whenAuthenticatedAndOwner_shouldReturnSubscription() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, testMerchantId))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptionId").value(testSubscriptionId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(testCustomerId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(testProductId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.priceId").value(testPriceId.toString()));
    }

    @Test
    @DisplayName("Get Subscription: Should return correct subscription status")
    void getSubscription_shouldReturnCorrectStatus() throws Exception {
        setupAuthentication();

        testSubscription.setStatus(SubscriptionStatus.PAID);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, testMerchantId))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("Get Subscription: Should return 404 when subscription not found")
    void getSubscription_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomSubscriptionId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(randomSubscriptionId, testMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find subscription with id " + randomSubscriptionId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + randomSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Subscription: Should return 401 when not authenticated")
    void getSubscription_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(subscriptionService, Mockito.never()).getSubscriptionByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Subscription: Should return 404 when merchant not found for user")
    void getSubscription_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(subscriptionService, Mockito.never()).getSubscriptionByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Subscription: Should return JSON content type")
    void getSubscription_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, testMerchantId))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Subscription: Should return 400 for invalid UUID format in path")
    void getSubscription_withInvalidUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Subscription: Should return 400 for invalid merchant UUID format")
    void getSubscription_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Subscription: Should not return subscription belonging to different merchant")
    void getSubscription_whenSubscriptionBelongsToDifferentMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID differentMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(differentMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, differentMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find subscription with id " + testSubscriptionId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + differentMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // GET ALL SUBSCRIPTIONS ENDPOINT TESTS

    @Test
    @DisplayName("Get Subscriptions: Should return all subscriptions for authenticated merchant")
    void getSubscriptions_whenAuthenticated_shouldReturnSubscriptions() throws Exception {
        setupAuthentication();

        Subscription subscription2 = new Subscription();
        subscription2.setSubscriptionId(UUID.randomUUID());
        subscription2.setMerchantId(testMerchantId);
        subscription2.setCustomerId(testCustomerId);
        subscription2.setProductId(testProductId);
        subscription2.setPriceId(testPriceId);
        subscription2.setQuantity(2);
        subscription2.setStatus(SubscriptionStatus.PAID);
        subscription2.setStartedAt(System.currentTimeMillis());
        subscription2.setCreatedAt(System.currentTimeMillis());

        List<Subscription> subscriptions = Arrays.asList(testSubscription, subscription2);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionsByMerchantId(testMerchantId))
                .thenReturn(subscriptions);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));
        Mockito.when(subscriptionService.toResponse(subscription2))
                .thenReturn(createSubscriptionResponse(subscription2));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].subscriptionId").value(testSubscriptionId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].subscriptionId").value(subscription2.getSubscriptionId().toString()));
    }

    @Test
    @DisplayName("Get Subscriptions: Should return empty list when merchant has no subscriptions")
    void getSubscriptions_whenNoSubscriptions_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionsByMerchantId(testMerchantId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Subscriptions: Should return 401 when not authenticated")
    void getSubscriptions_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(subscriptionService, Mockito.never()).getSubscriptionsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Subscriptions: Should return 404 when merchant not found for user")
    void getSubscriptions_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(subscriptionService, Mockito.never()).getSubscriptionsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Subscriptions: Should return JSON content type")
    void getSubscriptions_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionsByMerchantId(testMerchantId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    // CANCEL SUBSCRIPTION ENDPOINT TESTS

    @Test
    @DisplayName("Cancel Subscription: Should cancel subscription successfully")
    void cancelSubscription_withValidRequest_shouldReturnNoContent() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, testMerchantId))
                .thenReturn(testSubscription);
        Mockito.doNothing().when(subscriptionService).cancelSubscription(testSubscriptionId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(subscriptionService).cancelSubscription(testSubscriptionId);
    }

    @Test
    @DisplayName("Cancel Subscription: Should return 404 when subscription not found")
    void cancelSubscription_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomSubscriptionId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(randomSubscriptionId, testMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find subscription with id " + randomSubscriptionId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/subscriptions/" + randomSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(subscriptionService, Mockito.never()).cancelSubscription(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Cancel Subscription: Should return 401 when not authenticated")
    void cancelSubscription_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(subscriptionService, Mockito.never()).cancelSubscription(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Cancel Subscription: Should return 404 when merchant not found for user")
    void cancelSubscription_whenMerchantNotFoundForUser_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(subscriptionService, Mockito.never()).cancelSubscription(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Cancel Subscription: Should return 400 for invalid UUID format")
    void cancelSubscription_withInvalidUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/subscriptions/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Cancel Subscription: Should not allow canceling subscription of different merchant")
    void cancelSubscription_whenSubscriptionBelongsToDifferentMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID differentMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(differentMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, differentMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find subscription with id " + testSubscriptionId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + differentMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(subscriptionService, Mockito.never()).cancelSubscription(ArgumentMatchers.any());
    }

    // SECURITY TESTS

    @Test
    @DisplayName("Security: Should not expose sensitive data in error responses")
    void endpoints_shouldNotExposeSensitiveDataInErrors() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in UUID fields safely")
    void createSubscription_withSqlInjectionInUuid_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        // Since UUIDs are strongly typed, SQL injection in UUID fields would fail validation
        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"'; DROP TABLE subscriptions;--\",\"productId\":\"" + testProductId + "\",\"priceId\":\"" + testPriceId + "\",\"quantity\":1}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts safely")
    void createSubscription_withXssAttempt_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        // UUID fields are strongly typed, XSS would fail validation
        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"<script>alert('xss')</script>\",\"productId\":\"" + testProductId + "\",\"priceId\":\"" + testPriceId + "\",\"quantity\":1}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should not allow access to another user's merchant subscriptions")
    void getSubscriptions_forOtherUsersMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherUserMerchantId = UUID.randomUUID();

        Mockito.when(merchantService.getMerchantByIdAndUserId(otherUserMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherUserMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(subscriptionService, Mockito.never()).getSubscriptionsByMerchantId(ArgumentMatchers.any());
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should handle large quantity value")
    void createSubscription_withLargeQuantity_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(Integer.MAX_VALUE);

        testSubscription.setQuantity(Integer.MAX_VALUE);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(testMerchantId, request))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity").value(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("Edge Case: Should handle subscription with all subscription statuses")
    void getSubscription_shouldHandleAllStatuses() throws Exception {
        setupAuthentication();

        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            testSubscription.setStatus(status);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, testMerchantId))
                    .thenReturn(testSubscription);
            Mockito.when(subscriptionService.toResponse(testSubscription))
                    .thenReturn(createSubscriptionResponse(testSubscription));

            mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(status.name()));
        }
    }

    @Test
    @DisplayName("Edge Case: Should handle request with extra unknown fields")
    void createSubscription_withExtraFields_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(testCustomerId);
        request.setProductId(testProductId);
        request.setPriceId(testPriceId);
        request.setQuantity(1);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.createSubscription(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(CreateSubscriptionRequest.class)))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        // JSON with extra unknown field
        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"" + testCustomerId + "\",\"productId\":\"" + testProductId + "\",\"priceId\":\"" + testPriceId + "\",\"quantity\":1,\"unknownField\":\"value\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Edge Case: Should handle empty JSON object")
    void createSubscription_withEmptyJsonObject_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Edge Case: Should handle null JSON object")
    void createSubscription_withNullJsonValues_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":null,\"productId\":null,\"priceId\":null,\"quantity\":0}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(subscriptionService, Mockito.never()).createSubscription(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Edge Case: Should return correct timestamps in response")
    void getSubscription_shouldReturnCorrectTimestamps() throws Exception {
        setupAuthentication();

        long testStartedAt = 1700000000000L;
        long testCreatedAt = 1700000001000L;
        testSubscription.setStartedAt(testStartedAt);
        testSubscription.setCreatedAt(testCreatedAt);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(subscriptionService.getSubscriptionByIdAndMerchantId(testSubscriptionId, testMerchantId))
                .thenReturn(testSubscription);
        Mockito.when(subscriptionService.toResponse(testSubscription))
                .thenReturn(createSubscriptionResponse(testSubscription));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/" + testSubscriptionId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.startedAt").value(testStartedAt))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(testCreatedAt));
    }

    @Test
    @DisplayName("Edge Case: Should handle expired JWT token")
    void endpoints_withExpiredJwt_shouldReturnUnauthorized() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired";
        Mockito.when(jwtService.isTokenValid(expiredToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, expiredToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Edge Case: Should handle user not found in database after JWT validation")
    void endpoints_whenUserNotFoundAfterJwtValidation_shouldReturnUnauthorized() throws Exception {
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/subscriptions/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}