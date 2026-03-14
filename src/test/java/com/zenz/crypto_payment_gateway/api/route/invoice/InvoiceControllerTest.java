package com.zenz.crypto_payment_gateway.api.route.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.GlobalExceptionHandler;
import com.zenz.crypto_payment_gateway.api.config.JWTAuthenticationFilter;
import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.invoice.request.CreateInvoiceRequest;
import com.zenz.crypto_payment_gateway.api.route.invoice.response.InvoiceResponse;
import com.zenz.crypto_payment_gateway.entity.Customer;
import com.zenz.crypto_payment_gateway.entity.Invoice;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.enums.InvoiceStatus;
import com.zenz.crypto_payment_gateway.repository.UserRepository;
import com.zenz.crypto_payment_gateway.service.InvoiceService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@WebMvcTest(InvoiceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

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
    private Invoice testInvoice;
    private String testToken;
    private UUID testMerchantId;
    private UUID testCustomerId;
    private UUID testInvoiceId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testInvoiceId = UUID.randomUUID();

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
        testCustomer.setEmail("customer@example.com");

        testInvoice = new Invoice();
        testInvoice.setInvoiceId(testInvoiceId);
        testInvoice.setMerchantId(testMerchantId);
        testInvoice.setCustomerId(testCustomerId);
        testInvoice.setAmountDue(10000);
        testInvoice.setAmountPaid(0);
        testInvoice.setCurrency("USD");
        testInvoice.setStatus(InvoiceStatus.DRAFT);
        testInvoice.setAttempts(0);
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
     * Helper method to create an InvoiceResponse object.
     */
    private InvoiceResponse createInvoiceResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setInvoiceId(invoice.getInvoiceId());
        response.setCustomerId(invoice.getCustomerId());
        response.setAmountDue(invoice.getAmountDue());
        response.setAmountPaid(invoice.getAmountPaid());
        response.setCurrency(invoice.getCurrency());
        response.setAttempts(invoice.getAttempts());
        response.setLines(invoice.getLines());
        response.setStatus(invoice.getStatus());
        response.setCreatedAt(invoice.getCreatedAt());
        return response;
    }

    // CREATE INVOICE ENDPOINT TESTS

    @Test
    @DisplayName("Create Invoice: Should create invoice successfully with valid data")
    void createInvoice_withValidData_shouldReturnOk() throws Exception {
        setupAuthentication();

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(testInvoice);
        Mockito.when(invoiceService.toResponse(testInvoice)).thenReturn(createInvoiceResponse(testInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.invoiceId").value(testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountDue").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("DRAFT"));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(invoiceService).createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class));
    }

    @Test
    @DisplayName("Create Invoice: Should create invoice with metadata")
    void createInvoice_withMetadata_shouldReturnOk() throws Exception {
        setupAuthentication();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderId", "ORD-12345");
        metadata.put("description", "Test invoice");

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(5000);
        request.setCurrency("EUR");
        request.setCustomerId(testCustomerId);
        request.setMetadata(metadata);

        Invoice invoiceWithMetadata = new Invoice();
        invoiceWithMetadata.setInvoiceId(testInvoiceId);
        invoiceWithMetadata.setMerchantId(testMerchantId);
        invoiceWithMetadata.setCustomerId(testCustomerId);
        invoiceWithMetadata.setAmountDue(5000);
        invoiceWithMetadata.setCurrency("EUR");
        invoiceWithMetadata.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(invoiceWithMetadata);
        Mockito.when(invoiceService.toResponse(invoiceWithMetadata)).thenReturn(createInvoiceResponse(invoiceWithMetadata));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountDue").value(5000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("EUR"));
    }

    @Test
    @DisplayName("Create Invoice: Should reject request without authentication")
    void createInvoice_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request with invalid JWT token")
    void createInvoice_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request when user does not own merchant")
    void createInvoice_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + otherMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request with null amountDue")
    void createInvoice_withNullAmountDue_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        String json = "{\"currency\":\"USD\",\"customerId\":\"" + testCustomerId + "\"}";

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request with null currency")
    void createInvoice_withNullCurrency_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        String json = "{\"amountDue\":10000,\"customerId\":\"" + testCustomerId + "\"}";

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request with null customerId")
    void createInvoice_withNullCustomerId_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        String json = "{\"amountDue\":10000,\"currency\":\"USD\"}";

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should return 404 when customer not found for merchant")
    void createInvoice_whenCustomerNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID nonExistentCustomerId = UUID.randomUUID();
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(nonExistentCustomerId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenThrow(new ResourceNotFound("Failed to find customer with id " + nonExistentCustomerId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request with missing body")
    void createInvoice_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request with malformed JSON")
    void createInvoice_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject request with wrong content type")
    void createInvoice_withWrongContentType_shouldReturnUnsupportedMediaType() throws Exception {
        setupAuthentication();

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Create Invoice: Should reject zero amount")
    void createInvoice_withZeroAmount_shouldFail() throws Exception {
        setupAuthentication();

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(0);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        Invoice zeroInvoice = new Invoice();
        zeroInvoice.setInvoiceId(UUID.randomUUID());
        zeroInvoice.setMerchantId(testMerchantId);
        zeroInvoice.setCustomerId(testCustomerId);
        zeroInvoice.setAmountDue(0);
        zeroInvoice.setCurrency("USD");
        zeroInvoice.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(zeroInvoice);
        Mockito.when(invoiceService.toResponse(zeroInvoice)).thenReturn(createInvoiceResponse(zeroInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Create Invoice: Should accept large amount")
    void createInvoice_withLargeAmount_shouldSucceed() throws Exception {
        setupAuthentication();

        long largeAmount = 999999999999L;
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(largeAmount);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        Invoice largeInvoice = new Invoice();
        largeInvoice.setInvoiceId(UUID.randomUUID());
        largeInvoice.setMerchantId(testMerchantId);
        largeInvoice.setCustomerId(testCustomerId);
        largeInvoice.setAmountDue(largeAmount);
        largeInvoice.setCurrency("USD");
        largeInvoice.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(largeInvoice);
        Mockito.when(invoiceService.toResponse(largeInvoice)).thenReturn(createInvoiceResponse(largeInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountDue").value(largeAmount));
    }

    @Test
    @DisplayName("Create Invoice: Should accept different currency codes")
    void createInvoice_withDifferentCurrencies_shouldSucceed() throws Exception {
        setupAuthentication();

        String[] currencies = {"USD", "EUR", "GBP", "BTC", "ETH"};

        for (String currency : currencies) {
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setAmountDue(10000);
            request.setCurrency(currency);
            request.setCustomerId(testCustomerId);

            Invoice currencyInvoice = new Invoice();
            currencyInvoice.setInvoiceId(UUID.randomUUID());
            currencyInvoice.setMerchantId(testMerchantId);
            currencyInvoice.setCustomerId(testCustomerId);
            currencyInvoice.setAmountDue(10000);
            currencyInvoice.setCurrency(currency);
            currencyInvoice.setStatus(InvoiceStatus.DRAFT);

            Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                    .thenReturn(testMerchant);
            Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                    .thenReturn(currencyInvoice);
            Mockito.when(invoiceService.toResponse(currencyInvoice)).thenReturn(createInvoiceResponse(currencyInvoice));

            mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                    .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value(currency));
        }
    }

    // GET INVOICE BY ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Invoice: Should return invoice when authenticated and merchant owner")
    void getInvoice_whenAuthenticatedAndOwner_shouldReturnInvoice() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoiceByIdAndMerchantId(testInvoiceId, testMerchantId)).thenReturn(testInvoice);
        Mockito.when(invoiceService.toResponse(testInvoice)).thenReturn(createInvoiceResponse(testInvoice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.invoiceId").value(testInvoiceId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountDue").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("Get Invoice: Should return 404 when invoice not found")
    void getInvoice_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomInvoiceId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoiceByIdAndMerchantId(randomInvoiceId, testMerchantId))
                .thenThrow(new ResourceNotFound("Failed to find invoice with id " + randomInvoiceId + " for merchant"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/" + randomInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Invoice: Should return 401 when not authenticated")
    void getInvoice_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/" + testInvoiceId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(invoiceService, Mockito.never()).getInvoiceByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Invoice: Should return JSON content type")
    void getInvoice_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoiceByIdAndMerchantId(testInvoiceId, testMerchantId)).thenReturn(testInvoice);
        Mockito.when(invoiceService.toResponse(testInvoice)).thenReturn(createInvoiceResponse(testInvoice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Invoice: Should return 400 for invalid UUID format in invoiceId")
    void getInvoice_withInvalidInvoiceUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Invoice: Should return 400 for invalid UUID format in merchantId")
    void getInvoice_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Invoice: Should return 404 when user does not own merchant")
    void getInvoice_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherMerchantId + "/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(invoiceService, Mockito.never()).getInvoiceByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    // GET ALL INVOICES ENDPOINT TESTS

    @Test
    @DisplayName("Get Invoices: Should return all invoices for authenticated merchant owner")
    void getInvoices_whenAuthenticatedAndOwner_shouldReturnInvoices() throws Exception {
        setupAuthentication();

        Invoice invoice2 = new Invoice();
        invoice2.setInvoiceId(UUID.randomUUID());
        invoice2.setMerchantId(testMerchantId);
        invoice2.setCustomerId(testCustomerId);
        invoice2.setAmountDue(20000);
        invoice2.setCurrency("EUR");
        invoice2.setStatus(InvoiceStatus.OPEN);

        List<Invoice> invoices = Arrays.asList(testInvoice, invoice2);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoicesByMerchantId(testMerchantId)).thenReturn(invoices);
        Mockito.when(invoiceService.toResponse(testInvoice)).thenReturn(createInvoiceResponse(testInvoice));
        Mockito.when(invoiceService.toResponse(invoice2)).thenReturn(createInvoiceResponse(invoice2));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amountDue").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].amountDue").value(20000));
    }

    @Test
    @DisplayName("Get Invoices: Should return empty list when merchant has no invoices")
    void getInvoices_whenNoInvoices_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoicesByMerchantId(testMerchantId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Invoices: Should return 401 when not authenticated")
    void getInvoices_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(invoiceService, Mockito.never()).getInvoicesByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Invoices: Should return JSON content type")
    void getInvoices_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoicesByMerchantId(testMerchantId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Invoices: Should return 404 when user does not own merchant")
    void getInvoices_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(invoiceService, Mockito.never()).getInvoicesByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Invoices: Should return invoices with different statuses")
    void getInvoices_withDifferentStatuses_shouldReturnAllInvoices() throws Exception {
        setupAuthentication();

        Invoice draftInvoice = new Invoice();
        draftInvoice.setInvoiceId(UUID.randomUUID());
        draftInvoice.setMerchantId(testMerchantId);
        draftInvoice.setCustomerId(testCustomerId);
        draftInvoice.setAmountDue(5000);
        draftInvoice.setStatus(InvoiceStatus.DRAFT);

        Invoice openInvoice = new Invoice();
        openInvoice.setInvoiceId(UUID.randomUUID());
        openInvoice.setMerchantId(testMerchantId);
        openInvoice.setCustomerId(testCustomerId);
        openInvoice.setAmountDue(7500);
        openInvoice.setStatus(InvoiceStatus.OPEN);

        Invoice paidInvoice = new Invoice();
        paidInvoice.setInvoiceId(UUID.randomUUID());
        paidInvoice.setMerchantId(testMerchantId);
        paidInvoice.setCustomerId(testCustomerId);
        paidInvoice.setAmountDue(10000);
        paidInvoice.setAmountPaid(10000);
        paidInvoice.setStatus(InvoiceStatus.PAID);

        List<Invoice> invoices = Arrays.asList(draftInvoice, openInvoice, paidInvoice);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoicesByMerchantId(testMerchantId)).thenReturn(invoices);
        Mockito.when(invoiceService.toResponse(draftInvoice)).thenReturn(createInvoiceResponse(draftInvoice));
        Mockito.when(invoiceService.toResponse(openInvoice)).thenReturn(createInvoiceResponse(openInvoice));
        Mockito.when(invoiceService.toResponse(paidInvoice)).thenReturn(createInvoiceResponse(paidInvoice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("DRAFT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value("OPEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].status").value("PAID"));
    }

    // DELETE INVOICE ENDPOINT TESTS

    @Test
    @DisplayName("Delete Invoice: Should delete invoice successfully when authenticated and owner")
    void deleteInvoice_whenAuthenticatedAndOwner_shouldReturnNoContent() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.doNothing().when(invoiceService).deleteInvoice(testInvoiceId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(invoiceService).deleteInvoice(testInvoiceId);
    }

    @Test
    @DisplayName("Delete Invoice: Should return 404 when invoice not found")
    void deleteInvoice_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomInvoiceId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.doThrow(new ResourceNotFound("Failed to find invoice with id " + randomInvoiceId))
                .when(invoiceService).deleteInvoice(randomInvoiceId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/invoices/" + randomInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Delete Invoice: Should return 401 when not authenticated")
    void deleteInvoice_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/invoices/" + testInvoiceId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(invoiceService, Mockito.never()).deleteInvoice(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Delete Invoice: Should return 404 when user does not own merchant")
    void deleteInvoice_whenUserDoesNotOwnMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherMerchantId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(otherMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + otherMerchantId + "/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(invoiceService, Mockito.never()).deleteInvoice(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Delete Invoice: Should return 400 for invalid UUID format")
    void deleteInvoice_withInvalidUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.delete("/merchants/" + testMerchantId + "/invoices/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // SECURITY TESTS

    @Test
    @DisplayName("Security: Should not expose sensitive data in error responses")
    void endpoints_shouldNotExposeSensitiveDataInErrors() throws Exception {
        setupAuthentication();

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

//    @Test
//    @DisplayName("Security: Should handle SQL injection attempts in customerId safely")
//    void createInvoice_withSqlInjectionInCustomerId_shouldBeHandledSafely() throws Exception {
//        setupAuthentication();
//
//        CreateInvoiceRequest request = new CreateInvoiceRequest();
//        request.setAmountDue(10000);
//        request.setCurrency("USD");
//        request.setCustomerId("'; DROP TABLE invoices;--");
//
//        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
//                .thenReturn(testMerchant);
//        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
//                .thenThrow(new ResourceNotFound("Failed to find customer"));
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
//                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(MockMvcResultMatchers.status().isNotFound());
//    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in currency safely")
    void createInvoice_withSqlInjectionInCurrency_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD'; DROP TABLE invoices;--");
        request.setCustomerId(testCustomerId);

        Invoice sqlInvoice = new Invoice();
        sqlInvoice.setInvoiceId(UUID.randomUUID());
        sqlInvoice.setMerchantId(testMerchantId);
        sqlInvoice.setCustomerId(testCustomerId);
        sqlInvoice.setAmountDue(10000);
        sqlInvoice.setCurrency("USD'; DROP TABLE invoices;--");
        sqlInvoice.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(sqlInvoice);
        Mockito.when(invoiceService.toResponse(sqlInvoice)).thenReturn(createInvoiceResponse(sqlInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts in metadata safely")
    void createInvoice_withXssInMetadata_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("xss", "<script>alert('xss')</script>");

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);
        request.setMetadata(metadata);

        Invoice xssInvoice = new Invoice();
        xssInvoice.setInvoiceId(UUID.randomUUID());
        xssInvoice.setMerchantId(testMerchantId);
        xssInvoice.setCustomerId(testCustomerId);
        xssInvoice.setAmountDue(10000);
        xssInvoice.setCurrency("USD");
        xssInvoice.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(xssInvoice);
        Mockito.when(invoiceService.toResponse(xssInvoice)).thenReturn(createInvoiceResponse(xssInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should reject negative amount")
    void createInvoice_withNegativeAmount_shouldFail() throws Exception {
        setupAuthentication();

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(-10000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);

        Invoice negativeInvoice = new Invoice();
        negativeInvoice.setInvoiceId(UUID.randomUUID());
        negativeInvoice.setMerchantId(testMerchantId);
        negativeInvoice.setCustomerId(testCustomerId);
        negativeInvoice.setAmountDue(-10000);
        negativeInvoice.setCurrency("USD");
        negativeInvoice.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(negativeInvoice);
        Mockito.when(invoiceService.toResponse(negativeInvoice)).thenReturn(createInvoiceResponse(negativeInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case: Should handle complex metadata")
    void createInvoice_withComplexMetadata_shouldSucceed() throws Exception {
        setupAuthentication();

        Map<String, Object> nestedMetadata = new HashMap<>();
        nestedMetadata.put("orderId", "ORD-12345");
        nestedMetadata.put("customer", Map.of("name", "John Doe", "email", "john@example.com"));
        nestedMetadata.put("items", Arrays.asList("item1", "item2", "item3"));

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(15000);
        request.setCurrency("USD");
        request.setCustomerId(testCustomerId);
        request.setMetadata(nestedMetadata);

        Invoice complexInvoice = new Invoice();
        complexInvoice.setInvoiceId(UUID.randomUUID());
        complexInvoice.setMerchantId(testMerchantId);
        complexInvoice.setCustomerId(testCustomerId);
        complexInvoice.setAmountDue(15000);
        complexInvoice.setCurrency("USD");
        complexInvoice.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(complexInvoice);
        Mockito.when(invoiceService.toResponse(complexInvoice)).thenReturn(createInvoiceResponse(complexInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Edge Case: Should handle lowercase currency code")
    void createInvoice_withLowercaseCurrency_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setAmountDue(10000);
        request.setCurrency("usd");
        request.setCustomerId(testCustomerId);

        Invoice lowercaseInvoice = new Invoice();
        lowercaseInvoice.setInvoiceId(UUID.randomUUID());
        lowercaseInvoice.setMerchantId(testMerchantId);
        lowercaseInvoice.setCustomerId(testCustomerId);
        lowercaseInvoice.setAmountDue(10000);
        lowercaseInvoice.setCurrency("usd");
        lowercaseInvoice.setStatus(InvoiceStatus.DRAFT);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.createInvoice(ArgumentMatchers.eq(testMerchantId), ArgumentMatchers.any(CreateInvoiceRequest.class)))
                .thenReturn(lowercaseInvoice);
        Mockito.when(invoiceService.toResponse(lowercaseInvoice)).thenReturn(createInvoiceResponse(lowercaseInvoice));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("usd"));
    }

    @Test
    @DisplayName("Edge Case: Should handle invalid customerId format")
    void createInvoice_withInvalidCustomerIdFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        String invalidJson = "{\"amountDue\":10000,\"currency\":\"USD\",\"customerId\":\"not-a-uuid\"}";

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/invoices/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(invoiceService, Mockito.never()).createInvoice(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Edge Case: Should handle invoice with partial payment")
    void getInvoice_withPartialPayment_shouldReturnCorrectAmounts() throws Exception {
        setupAuthentication();

        Invoice partialInvoice = new Invoice();
        partialInvoice.setInvoiceId(testInvoiceId);
        partialInvoice.setMerchantId(testMerchantId);
        partialInvoice.setCustomerId(testCustomerId);
        partialInvoice.setAmountDue(10000);
        partialInvoice.setAmountPaid(5000);
        partialInvoice.setCurrency("USD");
        partialInvoice.setStatus(InvoiceStatus.OPEN);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoiceByIdAndMerchantId(testInvoiceId, testMerchantId)).thenReturn(partialInvoice);
        Mockito.when(invoiceService.toResponse(partialInvoice)).thenReturn(createInvoiceResponse(partialInvoice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountDue").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountPaid").value(5000));
    }

    @Test
    @DisplayName("Edge Case: Should handle invoice with full payment")
    void getInvoice_withFullPayment_shouldReturnPaidStatus() throws Exception {
        setupAuthentication();

        Invoice paidInvoice = new Invoice();
        paidInvoice.setInvoiceId(testInvoiceId);
        paidInvoice.setMerchantId(testMerchantId);
        paidInvoice.setCustomerId(testCustomerId);
        paidInvoice.setAmountDue(10000);
        paidInvoice.setAmountPaid(10000);
        paidInvoice.setCurrency("USD");
        paidInvoice.setStatus(InvoiceStatus.PAID);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(invoiceService.getInvoiceByIdAndMerchantId(testInvoiceId, testMerchantId)).thenReturn(paidInvoice);
        Mockito.when(invoiceService.toResponse(paidInvoice)).thenReturn(createInvoiceResponse(paidInvoice));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/invoices/" + testInvoiceId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountDue").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amountPaid").value(10000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PAID"));
    }
}