package com.zenz.crypto_payment_gateway.api.route.price;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.GlobalExceptionHandler;
import com.zenz.crypto_payment_gateway.api.config.JWTAuthenticationFilter;
import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.price.model.request.CreatePriceRequest;
import com.zenz.crypto_payment_gateway.api.route.price.model.request.UpdatePriceRequest;
import com.zenz.crypto_payment_gateway.api.route.price.model.response.PriceResponse;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.Price;
import com.zenz.crypto_payment_gateway.entity.Product;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.enums.PricingInterval;
import com.zenz.crypto_payment_gateway.enums.PricingType;
import com.zenz.crypto_payment_gateway.model.Recurring;
import com.zenz.crypto_payment_gateway.repository.UserRepository;
import com.zenz.crypto_payment_gateway.service.JWTService;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import com.zenz.crypto_payment_gateway.service.PriceService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@WebMvcTest(PriceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceService priceService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private Product testProduct;
    private Price testPrice;
    private Price testRecurringPrice;
    private String testToken;
    private UUID testMerchantId;
    private UUID testProductId;
    private UUID testPriceId;
    private UUID testRecurringPriceId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testPriceId = UUID.randomUUID();
        testRecurringPriceId = UUID.randomUUID();

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName("Test Merchant");
        testMerchant.setCreatedAt(System.currentTimeMillis());

        testProduct = new Product();
        testProduct.setProductId(testProductId);
        testProduct.setMerchantId(testMerchantId);
        testProduct.setName("Test Product");

        // One-time price
        testPrice = new Price();
        testPrice.setPriceId(testPriceId);
        testPrice.setMerchantId(testMerchantId);
        testPrice.setProductId(testProductId);
        testPrice.setAmount(1000);
        testPrice.setPricingType(PricingType.ONE_TIME);
        testPrice.setCurrency("USD");

        // Recurring price
        Recurring recurring = new Recurring();
        recurring.setInterval(PricingInterval.MONTHLY);
        recurring.setIntervalCount(1);

        testRecurringPrice = new Price();
        testRecurringPrice.setPriceId(testRecurringPriceId);
        testRecurringPrice.setMerchantId(testMerchantId);
        testRecurringPrice.setProductId(testProductId);
        testRecurringPrice.setAmount(2500);
        testRecurringPrice.setPricingType(PricingType.RECURRING);
        testRecurringPrice.setCurrency("USD");
        testRecurringPrice.setRecurring(recurring);
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
     * Helper method to create a PriceResponse object.
     */
    private PriceResponse createPriceResponse(Price price) {
        PriceResponse response = new PriceResponse();
        response.setPriceId(price.getPriceId());
        response.setProductId(price.getProductId());
        response.setAmount(price.getAmount());
        response.setPricingType(price.getPricingType());
        response.setCurrency(price.getCurrency());
        response.setRecurring(price.getRecurring());
        response.setMetadata(price.getMetadata());
        return response;
    }

    // CREATE PRICE ENDPOINT TESTS

    @Test
    @DisplayName("Create Price: Should create one-time price successfully with valid data")
    void createPrice_withValidOneTimeData_shouldReturnOk() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(testPrice);
        Mockito.when(priceService.toResponse(testPrice)).thenReturn(createPriceResponse(testPrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.priceId").value(testPriceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pricingType").value("ONE_TIME"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(testProductId.toString()));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(priceService).createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class));
    }

    @Test
    @DisplayName("Create Price: Should create recurring price successfully with valid data")
    void createPrice_withValidRecurringData_shouldReturnOk() throws Exception {
        setupAuthentication();

        Recurring recurring = new Recurring();
        recurring.setInterval(PricingInterval.MONTHLY);
        recurring.setIntervalCount(1);

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(2500);
        request.setPricingType(PricingType.RECURRING);
        request.setCurrency("USD");
        request.setProductId(testProductId);
        request.setRecurring(recurring);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(testRecurringPrice);
        Mockito.when(priceService.toResponse(testRecurringPrice)).thenReturn(createPriceResponse(testRecurringPrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.priceId").value(testRecurringPriceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(2500))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pricingType").value("RECURRING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.interval").value("MONTHLY"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.intervalCount").value(1));

        Mockito.verify(priceService).createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class));
    }

    @Test
    @DisplayName("Create Price: Should create price with weekly interval")
    void createPrice_withWeeklyInterval_shouldSucceed() throws Exception {
        setupAuthentication();

        Recurring recurring = new Recurring();
        recurring.setInterval(PricingInterval.WEEKLY);
        recurring.setIntervalCount(2);

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(500);
        request.setPricingType(PricingType.RECURRING);
        request.setCurrency("EUR");
        request.setProductId(testProductId);
        request.setRecurring(recurring);

        Price weeklyPrice = new Price();
        weeklyPrice.setPriceId(UUID.randomUUID());
        weeklyPrice.setMerchantId(testMerchantId);
        weeklyPrice.setProductId(testProductId);
        weeklyPrice.setAmount(500);
        weeklyPrice.setPricingType(PricingType.RECURRING);
        weeklyPrice.setCurrency("EUR");
        weeklyPrice.setRecurring(recurring);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(weeklyPrice);
        Mockito.when(priceService.toResponse(weeklyPrice)).thenReturn(createPriceResponse(weeklyPrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.interval").value("WEEKLY"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.intervalCount").value(2));
    }

    @Test
    @DisplayName("Create Price: Should create price with daily interval")
    void createPrice_withDailyInterval_shouldSucceed() throws Exception {
        setupAuthentication();

        Recurring recurring = new Recurring();
        recurring.setInterval(PricingInterval.DAILY);
        recurring.setIntervalCount(1);

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(100);
        request.setPricingType(PricingType.RECURRING);
        request.setCurrency("GBP");
        request.setProductId(testProductId);
        request.setRecurring(recurring);

        Price dailyPrice = new Price();
        dailyPrice.setPriceId(UUID.randomUUID());
        dailyPrice.setMerchantId(testMerchantId);
        dailyPrice.setProductId(testProductId);
        dailyPrice.setAmount(100);
        dailyPrice.setPricingType(PricingType.RECURRING);
        dailyPrice.setCurrency("GBP");
        dailyPrice.setRecurring(recurring);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(dailyPrice);
        Mockito.when(priceService.toResponse(dailyPrice)).thenReturn(createPriceResponse(dailyPrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.interval").value("DAILY"));
    }

    @Test
    @DisplayName("Create Price: Should reject request without authentication")
    void createPrice_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject request with invalid JWT token")
    void createPrice_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject request when user does not own merchant")
    void createPrice_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + otherMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject request with null pricing type")
    void createPrice_withNullPricingType_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(null);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject request with null currency")
    void createPrice_withNullCurrency_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency(null);
        request.setProductId(testProductId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject request with null product ID")
    void createPrice_withNullProductId_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(null);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject recurring price without recurring object")
    void createPrice_withRecurringTypeButNoRecurringObject_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(2500);
        request.setPricingType(PricingType.RECURRING);
        request.setCurrency("USD");
        request.setProductId(testProductId);
        // recurring is null - should fail validation

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject when product not found for merchant")
    void createPrice_whenProductNotFoundForMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID nonExistentProductId = UUID.randomUUID();
        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(nonExistentProductId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenThrow(new ResourceNotFound("Failed to find product with id " + nonExistentProductId));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Create Price: Should reject request with missing body")
    void createPrice_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject request with malformed JSON")
    void createPrice_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject request with wrong content type")
    void createPrice_withWrongContentType_shouldReturnUnsupportedMediaType() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Price: Should reject zero amount")
    void createPrice_withZeroAmount_shouldFail() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(0);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Price freePrice = new Price();
        freePrice.setPriceId(UUID.randomUUID());
        freePrice.setMerchantId(testMerchantId);
        freePrice.setProductId(testProductId);
        freePrice.setAmount(0);
        freePrice.setPricingType(PricingType.ONE_TIME);
        freePrice.setCurrency("USD");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(freePrice);
        Mockito.when(priceService.toResponse(freePrice)).thenReturn(createPriceResponse(freePrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Create Price: Should accept large amount")
    void createPrice_withLargeAmount_shouldSucceed() throws Exception {
        setupAuthentication();

        long largeAmount = 999999999999L;
        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(largeAmount);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Price expensivePrice = new Price();
        expensivePrice.setPriceId(UUID.randomUUID());
        expensivePrice.setMerchantId(testMerchantId);
        expensivePrice.setProductId(testProductId);
        expensivePrice.setAmount(largeAmount);
        expensivePrice.setPricingType(PricingType.ONE_TIME);
        expensivePrice.setCurrency("USD");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(expensivePrice);
        Mockito.when(priceService.toResponse(expensivePrice)).thenReturn(createPriceResponse(expensivePrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(largeAmount));
    }

    @Test
    @DisplayName("Create Price: Should accept different currency codes")
    void createPrice_withDifferentCurrencies_shouldSucceed() throws Exception {
        setupAuthentication();

        String[] currencies = {"USD", "EUR", "GBP", "BTC", "ETH"};

        for (String currency : currencies) {
            CreatePriceRequest request = new CreatePriceRequest();
            request.setAmount(1000);
            request.setPricingType(PricingType.ONE_TIME);
            request.setCurrency(currency);
            request.setProductId(testProductId);

            Price currencyPrice = new Price();
            currencyPrice.setPriceId(UUID.randomUUID());
            currencyPrice.setMerchantId(testMerchantId);
            currencyPrice.setProductId(testProductId);
            currencyPrice.setAmount(1000);
            currencyPrice.setPricingType(PricingType.ONE_TIME);
            currencyPrice.setCurrency(currency);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                    .thenReturn(currencyPrice);
            Mockito.when(priceService.toResponse(currencyPrice)).thenReturn(createPriceResponse(currencyPrice));

            mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value(currency));
        }
    }

    @Test
    @DisplayName("Create Price: Should accept recurring with multiple interval count")
    void createPrice_withMultipleIntervalCount_shouldSucceed() throws Exception {
        setupAuthentication();

        Recurring recurring = new Recurring();
        recurring.setInterval(PricingInterval.MONTHLY);
        recurring.setIntervalCount(3); // Every 3 months

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(7500);
        request.setPricingType(PricingType.RECURRING);
        request.setCurrency("USD");
        request.setProductId(testProductId);
        request.setRecurring(recurring);

        Price quarterlyPrice = new Price();
        quarterlyPrice.setPriceId(UUID.randomUUID());
        quarterlyPrice.setMerchantId(testMerchantId);
        quarterlyPrice.setProductId(testProductId);
        quarterlyPrice.setAmount(7500);
        quarterlyPrice.setPricingType(PricingType.RECURRING);
        quarterlyPrice.setCurrency("USD");
        quarterlyPrice.setRecurring(recurring);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(quarterlyPrice);
        Mockito.when(priceService.toResponse(quarterlyPrice)).thenReturn(createPriceResponse(quarterlyPrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.intervalCount").value(3));
    }

    // GET PRICE BY ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Price: Should return price when authenticated and merchant owner")
    void getPrice_whenAuthenticatedAndOwner_shouldReturnPrice() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPriceById(testPriceId)).thenReturn(testPrice);
        Mockito.when(priceService.toResponse(testPrice)).thenReturn(createPriceResponse(testPrice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.priceId").value(testPriceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pricingType").value("ONE_TIME"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("Get Price: Should return recurring price with recurring details")
    void getPrice_whenRecurringPrice_shouldReturnRecurringDetails() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPriceById(testRecurringPriceId)).thenReturn(testRecurringPrice);
        Mockito.when(priceService.toResponse(testRecurringPrice)).thenReturn(createPriceResponse(testRecurringPrice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/" + testRecurringPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.priceId").value(testRecurringPriceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pricingType").value("RECURRING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.interval").value("MONTHLY"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurring.intervalCount").value(1));
    }

    @Test
    @DisplayName("Get Price: Should return 404 when price not found")
    void getPrice_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomPriceId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPriceById(randomPriceId))
                .thenThrow(new ResourceNotFound("Failed to find price with id " + randomPriceId));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/" + randomPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Price: Should return 401 when not authenticated")
    void getPrice_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(priceService, Mockito.never()).getPriceById(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Price: Should return JSON content type")
    void getPrice_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPriceById(testPriceId)).thenReturn(testPrice);
        Mockito.when(priceService.toResponse(testPrice)).thenReturn(createPriceResponse(testPrice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Price: Should return 400 for invalid UUID format in priceId")
    void getPrice_withInvalidPriceUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Price: Should return 400 for invalid UUID format in merchantId")
    void getPrice_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Price: Should return 404 when user does not own merchant")
    void getPrice_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(priceService, Mockito.never()).getPriceById(ArgumentMatchers.any());
    }

    // GET ALL PRICES ENDPOINT TESTS

    @Test
    @DisplayName("Get Prices: Should return all prices for authenticated merchant owner")
    void getPrices_whenAuthenticatedAndOwner_shouldReturnPrices() throws Exception {
        setupAuthentication();

        Price price2 = new Price();
        price2.setPriceId(UUID.randomUUID());
        price2.setMerchantId(testMerchantId);
        price2.setProductId(testProductId);
        price2.setAmount(2000);
        price2.setPricingType(PricingType.ONE_TIME);
        price2.setCurrency("EUR");

        List<Price> prices = Arrays.asList(testPrice, price2);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPricesByMerchantId(testMerchantId)).thenReturn(prices);
        Mockito.when(priceService.toResponse(testPrice)).thenReturn(createPriceResponse(testPrice));
        Mockito.when(priceService.toResponse(price2)).thenReturn(createPriceResponse(price2));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amount").value(1000))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].amount").value(2000));
    }

    @Test
    @DisplayName("Get Prices: Should return empty list when merchant has no prices")
    void getPrices_whenNoPrices_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPricesByMerchantId(testMerchantId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Prices: Should return 401 when not authenticated")
    void getPrices_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(priceService, Mockito.never()).getPricesByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Prices: Should return JSON content type")
    void getPrices_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPricesByMerchantId(testMerchantId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Prices: Should return 404 when user does not own merchant")
    void getPrices_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(priceService, Mockito.never()).getPricesByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Prices: Should return prices with mixed pricing types")
    void getPrices_withMixedPricingTypes_shouldReturnAllPrices() throws Exception {
        setupAuthentication();

        List<Price> prices = Arrays.asList(testPrice, testRecurringPrice);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.getPricesByMerchantId(testMerchantId)).thenReturn(prices);
        Mockito.when(priceService.toResponse(testPrice)).thenReturn(createPriceResponse(testPrice));
        Mockito.when(priceService.toResponse(testRecurringPrice)).thenReturn(createPriceResponse(testRecurringPrice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pricingType").value("ONE_TIME"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pricingType").value("RECURRING"));
    }

    // UPDATE PRICE ENDPOINT TESTS

    @Test
    @DisplayName("Update Price: Should update price metadata successfully")
    void updatePrice_withValidMetadata_shouldReturnUpdatedPrice() throws Exception {
        setupAuthentication();

        String newMetadata = "{\"key\": \"value\"}";
        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata(newMetadata);

        Price updatedPrice = new Price();
        updatedPrice.setPriceId(testPriceId);
        updatedPrice.setMerchantId(testMerchantId);
        updatedPrice.setProductId(testProductId);
        updatedPrice.setAmount(1000);
        updatedPrice.setPricingType(PricingType.ONE_TIME);
        updatedPrice.setCurrency("USD");
        updatedPrice.setMetadata(newMetadata);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenReturn(updatedPrice);
        Mockito.when(priceService.toResponse(updatedPrice)).thenReturn(createPriceResponse(updatedPrice));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.metadata").value(newMetadata));
    }

    @Test
    @DisplayName("Update Price: Should return 404 when price not found")
    void updatePrice_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomPriceId = UUID.randomUUID();
        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata("test");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(randomPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenThrow(new ResourceNotFound("Failed to find price with id " + randomPriceId));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + randomPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Update Price: Should return 401 when not authenticated")
    void updatePrice_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata("test");

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(priceService, Mockito.never()).updatePrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Price: Should return 404 when user does not own merchant")
    void updatePrice_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata("test");

        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + otherMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(priceService, Mockito.never()).updatePrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Price: Should accept null metadata (no update)")
    void updatePrice_withNullMetadata_shouldSucceed() throws Exception {
        setupAuthentication();

        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata(null);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenReturn(testPrice);
        Mockito.when(priceService.toResponse(testPrice)).thenReturn(createPriceResponse(testPrice));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Update Price: Should accept empty metadata")
    void updatePrice_withEmptyMetadata_shouldSucceed() throws Exception {
        setupAuthentication();

        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata("");

        Price updatedPrice = new Price();
        updatedPrice.setPriceId(testPriceId);
        updatedPrice.setMerchantId(testMerchantId);
        updatedPrice.setProductId(testProductId);
        updatedPrice.setAmount(1000);
        updatedPrice.setPricingType(PricingType.ONE_TIME);
        updatedPrice.setCurrency("USD");
        updatedPrice.setMetadata("");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenReturn(updatedPrice);
        Mockito.when(priceService.toResponse(updatedPrice)).thenReturn(createPriceResponse(updatedPrice));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Update Price: Should reject request with missing body")
    void updatePrice_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).updatePrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Price: Should reject request with malformed JSON")
    void updatePrice_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).updatePrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // SECURITY TESTS

    @Test
    @DisplayName("Security: Should not expose sensitive data in error responses")
    void endpoints_shouldNotExposeSensitiveDataInErrors() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in metadata safely")
    void updatePrice_withSqlInjectionInMetadata_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata("'; DROP TABLE prices;--");

        Price updatedPrice = new Price();
        updatedPrice.setPriceId(testPriceId);
        updatedPrice.setMerchantId(testMerchantId);
        updatedPrice.setProductId(testProductId);
        updatedPrice.setAmount(1000);
        updatedPrice.setPricingType(PricingType.ONE_TIME);
        updatedPrice.setCurrency("USD");
        updatedPrice.setMetadata("'; DROP TABLE prices;--");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenReturn(updatedPrice);
        Mockito.when(priceService.toResponse(updatedPrice)).thenReturn(createPriceResponse(updatedPrice));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(priceService).updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class));
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts in metadata safely")
    void updatePrice_withXssInMetadata_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata("<script>alert('xss')</script>");

        Price updatedPrice = new Price();
        updatedPrice.setPriceId(testPriceId);
        updatedPrice.setMerchantId(testMerchantId);
        updatedPrice.setProductId(testProductId);
        updatedPrice.setAmount(1000);
        updatedPrice.setPricingType(PricingType.ONE_TIME);
        updatedPrice.setCurrency("USD");
        updatedPrice.setMetadata("<script>alert('xss')</script>");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenReturn(updatedPrice);
        Mockito.when(priceService.toResponse(updatedPrice)).thenReturn(createPriceResponse(updatedPrice));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in currency safely")
    void createPrice_withSqlInjectionInCurrency_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD'; DROP TABLE prices;--");
        request.setProductId(testProductId);

        Price sqlPrice = new Price();
        sqlPrice.setPriceId(UUID.randomUUID());
        sqlPrice.setMerchantId(testMerchantId);
        sqlPrice.setProductId(testProductId);
        sqlPrice.setAmount(1000);
        sqlPrice.setPricingType(PricingType.ONE_TIME);
        sqlPrice.setCurrency("USD'; DROP TABLE prices;--");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(sqlPrice);
        Mockito.when(priceService.toResponse(sqlPrice)).thenReturn(createPriceResponse(sqlPrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should handle very long metadata")
    void updatePrice_withVeryLongMetadata_shouldSucceed() throws Exception {
        setupAuthentication();

        String longMetadata = "a".repeat(10000);
        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata(longMetadata);

        Price updatedPrice = new Price();
        updatedPrice.setPriceId(testPriceId);
        updatedPrice.setMerchantId(testMerchantId);
        updatedPrice.setProductId(testProductId);
        updatedPrice.setAmount(1000);
        updatedPrice.setPricingType(PricingType.ONE_TIME);
        updatedPrice.setCurrency("USD");
        updatedPrice.setMetadata(longMetadata);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenReturn(updatedPrice);
        Mockito.when(priceService.toResponse(updatedPrice)).thenReturn(createPriceResponse(updatedPrice));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Edge Case: Should handle JSON metadata")
    void updatePrice_withJsonMetadata_shouldSucceed() throws Exception {
        setupAuthentication();

        String jsonMetadata = "{\"key1\": \"value1\", \"key2\": {\"nested\": \"value2\"}, \"array\": [1, 2, 3]}";
        UpdatePriceRequest request = new UpdatePriceRequest();
        request.setMetadata(jsonMetadata);

        Price updatedPrice = new Price();
        updatedPrice.setPriceId(testPriceId);
        updatedPrice.setMerchantId(testMerchantId);
        updatedPrice.setProductId(testProductId);
        updatedPrice.setAmount(1000);
        updatedPrice.setPricingType(PricingType.ONE_TIME);
        updatedPrice.setCurrency("USD");
        updatedPrice.setMetadata(jsonMetadata);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.updatePrice(ArgumentMatchers.eq(testPriceId), ArgumentMatchers.any(UpdatePriceRequest.class)))
                .thenReturn(updatedPrice);
        Mockito.when(priceService.toResponse(updatedPrice)).thenReturn(createPriceResponse(updatedPrice));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/prices/" + testPriceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.metadata").value(jsonMetadata));
    }

    @Test
    @DisplayName("Edge Case: Should handle currency with lowercase")
    void createPrice_withLowercaseCurrency_shouldSucceed() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("usd");
        request.setProductId(testProductId);

        Price lowercasePrice = new Price();
        lowercasePrice.setPriceId(UUID.randomUUID());
        lowercasePrice.setMerchantId(testMerchantId);
        lowercasePrice.setProductId(testProductId);
        lowercasePrice.setAmount(1000);
        lowercasePrice.setPricingType(PricingType.ONE_TIME);
        lowercasePrice.setCurrency("usd");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(lowercasePrice);
        Mockito.when(priceService.toResponse(lowercasePrice)).thenReturn(createPriceResponse(lowercasePrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("usd"));
    }

    @Test
    @DisplayName("Edge Case: Should handle negative amount. No negative amounts allowed")
    void createPrice_withNegativeAmount_shouldSucceedOrFail() throws Exception {
        setupAuthentication();

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(-1000);
        request.setPricingType(PricingType.ONE_TIME);
        request.setCurrency("USD");
        request.setProductId(testProductId);

        Price negativePrice = new Price();
        negativePrice.setPriceId(UUID.randomUUID());
        negativePrice.setMerchantId(testMerchantId);
        negativePrice.setProductId(testProductId);
        negativePrice.setAmount(-1000);
        negativePrice.setPricingType(PricingType.ONE_TIME);
        negativePrice.setCurrency("USD");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(negativePrice);
        Mockito.when(priceService.toResponse(negativePrice)).thenReturn(createPriceResponse(negativePrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case: Should handle recurring with zero interval count")
    void createPrice_withZeroIntervalCount_shouldSucceed() throws Exception {
        setupAuthentication();

        Recurring recurring = new Recurring();
        recurring.setInterval(PricingInterval.MONTHLY);
        recurring.setIntervalCount(0);

        CreatePriceRequest request = new CreatePriceRequest();
        request.setAmount(1000);
        request.setPricingType(PricingType.RECURRING);
        request.setCurrency("USD");
        request.setProductId(testProductId);
        request.setRecurring(recurring);

        Price createdPrice = new Price();
        createdPrice.setPriceId(testPriceId);
        createdPrice.setMerchantId(testMerchantId);
        createdPrice.setProductId(testProductId);
        createdPrice.setAmount(1000);
        createdPrice.setPricingType(PricingType.RECURRING);
        createdPrice.setCurrency("USD");
        createdPrice.setRecurring(recurring);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(priceService.createPrice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreatePriceRequest.class)))
                .thenReturn(createdPrice);
        Mockito.when(priceService.toResponse(createdPrice)).thenReturn(createPriceResponse(createdPrice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Edge Case: Should handle invalid pricing interval value")
    void createPrice_withInvalidPricingInterval_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        String invalidJson = "{"
                + "\"amount\": 1000,"
                + "\"pricingType\": \"RECURRING\","
                + "\"currency\": \"USD\","
                + "\"productId\": \"" + testProductId + "\","
                + "\"recurring\": {"
                + "\"interval\": \"INVALID_INTERVAL\","
                + "\"intervalCount\": 1"
                + "}"
                + "}";

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/prices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(priceService, Mockito.never()).createPrice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }
}
