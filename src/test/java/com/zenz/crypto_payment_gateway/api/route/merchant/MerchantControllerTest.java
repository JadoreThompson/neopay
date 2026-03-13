package com.zenz.crypto_payment_gateway.api.route.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.CreateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.request.UpdateMerchantRequest;
import com.zenz.crypto_payment_gateway.api.route.merchant.response.MerchantResponse;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MerchantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MerchantService merchantService;

    @InjectMocks
    private MerchantController merchantController;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private String testMerchantId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(merchantController).build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        testMerchantId = UUID.randomUUID().toString();
        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName("Test Merchant");
        testMerchant.setDescription("A test merchant for testing");
        testMerchant.setUser(testUser);
    }

    @Test
    void createMerchant_shouldReturnMerchantResponse() throws Exception {
        // Arrange
        CreateMerchantRequest request = new CreateMerchantRequest();
        request.setName("New Merchant");
        request.setDescription("A new merchant");

        MerchantResponse expectedResponse = new MerchantResponse();
        expectedResponse.setMerchantId(UUID.randomUUID().toString());
        expectedResponse.setName("New Merchant");
        expectedResponse.setDescription("A new merchant");

        when(merchantService.createMerchant(any(User.class), any(CreateMerchantRequest.class)))
                .thenReturn(testMerchant);
        when(merchantService.toResponse(any(Merchant.class)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/merchants/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Merchant")))
                .andExpect(jsonPath("$.description", is("A new merchant")));
    }

    @Test
    void getMerchant_shouldReturnMerchantResponse() throws Exception {
        // Arrange
        MerchantResponse expectedResponse = new MerchantResponse();
        expectedResponse.setMerchantId(testMerchantId);
        expectedResponse.setName("Test Merchant");
        expectedResponse.setDescription("A test merchant for testing");

        when(merchantService.getMerchantByIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(testMerchant);
        when(merchantService.toResponse(any(Merchant.class)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/merchants/{merchantId}/", testMerchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId", is(testMerchantId)))
                .andExpect(jsonPath("$.name", is("Test Merchant")));
    }

    @Test
    void getMerchants_shouldReturnListOfMerchants() throws Exception {
        // Arrange
        MerchantResponse response1 = new MerchantResponse();
        response1.setMerchantId(testMerchantId);
        response1.setName("Test Merchant");

        when(merchantService.getMerchantsByUserId(any(UUID.class)))
                .thenReturn(List.of(testMerchant));
        when(merchantService.toResponse(any(Merchant.class)))
                .thenReturn(response1);

        // Act & Assert
        mockMvc.perform(get("/merchants/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Merchant")));
    }

    @Test
    void updateMerchant_shouldReturnUpdatedMerchant() throws Exception {
        // Arrange
        UpdateMerchantRequest request = new UpdateMerchantRequest();
        request.setName("Updated Merchant Name");
        request.setDescription("Updated description");

        Merchant updatedMerchant = new Merchant();
        updatedMerchant.setMerchantId(testMerchantId);
        updatedMerchant.setName("Updated Merchant Name");
        updatedMerchant.setDescription("Updated description");

        MerchantResponse expectedResponse = new MerchantResponse();
        expectedResponse.setMerchantId(testMerchantId);
        expectedResponse.setName("Updated Merchant Name");
        expectedResponse.setDescription("Updated description");

        when(merchantService.getMerchantByIdAndUserId(any(UUID.class), any(UUID.class)))
                .thenReturn(testMerchant);
        when(merchantService.updateMerchant(any(UUID.class), any(UpdateMerchantRequest.class)))
                .thenReturn(updatedMerchant);
        when(merchantService.toResponse(any(Merchant.class)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(put("/merchants/{merchantId}/", testMerchantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Merchant Name")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }
}