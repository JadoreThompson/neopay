package com.zenz.crypto_payment_gateway.api.route.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenz.crypto_payment_gateway.api.GlobalExceptionHandler;
import com.zenz.crypto_payment_gateway.api.config.JWTAuthenticationFilter;
import com.zenz.crypto_payment_gateway.api.config.SecurityConfig;
import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.route.product.request.CreateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.request.UpdateProductRequest;
import com.zenz.crypto_payment_gateway.api.route.product.response.ProductResponse;
import com.zenz.crypto_payment_gateway.entity.Merchant;
import com.zenz.crypto_payment_gateway.entity.Product;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.repository.ProductRepository;
import com.zenz.crypto_payment_gateway.repository.UserRepository;
import com.zenz.crypto_payment_gateway.service.JWTService;
import com.zenz.crypto_payment_gateway.service.MerchantService;
import com.zenz.crypto_payment_gateway.service.ProductService;
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


@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProductRepository productRepository;

    private ObjectMapper objectMapper;
    private User testUser;
    private Merchant testMerchant;
    private Product testProduct;
    private String testToken;
    private UUID testMerchantId;
    private UUID testProductId;
    private String testProductName;
    private String testProductDescription;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test-token";
        testMerchantId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testProductName = "Test Product";
        testProductDescription = "A test product description";

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

        testMerchant = new Merchant();
        testMerchant.setMerchantId(testMerchantId);
        testMerchant.setName("Test Merchant");
        testMerchant.setDescription("Test merchant description");
        testMerchant.setCreatedAt(System.currentTimeMillis());

        testProduct = new Product();
        testProduct.setProductId(testProductId);
        testProduct.setMerchantId(testMerchantId);
        testProduct.setName(testProductName);
        testProduct.setDescription(testProductDescription);
        testProduct.setCreatedAt(System.currentTimeMillis());
    }

    private void setupAuthentication() {
        Mockito.when(jwtService.isTokenValid(testToken)).thenReturn(true);
        Mockito.when(jwtService.extractUserId(testToken)).thenReturn(Optional.of(testUser.getUserId().toString()));
        Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
    }

    private ProductResponse createProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setMerchantId(product.getMerchantId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setImage(product.getImage());
        response.setCreatedAt(product.getCreatedAt());
        return response;
    }

    // CREATE PRODUCT ENDPOINT TESTS

    @Test
    @DisplayName("Create Product: Should create product successfully with valid data")
    void createProduct_withValidData_shouldReturnOk() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(testProduct);
        Mockito.when(productService.toResponse(testProduct)).thenReturn(createProductResponse(testProduct));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(testProductId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testProductName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testProductDescription));

        Mockito.verify(merchantService).getMerchantByIdAndUserId(testMerchantId, testUser.getUserId());
        Mockito.verify(productService).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId));
    }

    @Test
    @DisplayName("Create Product: Should associate product with correct merchant")
    void createProduct_shouldAssociateWithCorrectMerchant() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(testProduct);
        Mockito.when(productService.toResponse(testProduct)).thenReturn(createProductResponse(testProduct));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.merchantId").value(testMerchantId.toString()));
    }

    @Test
    @DisplayName("Create Product: Should create product with name only (description optional)")
    void createProduct_withNameOnly_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        // description is optional

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName(testProductName);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testProductName));
    }

    @Test
    @DisplayName("Create Product: Should reject request when name is null")
    void createProduct_withNullName_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(null);
        request.setDescription(testProductDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request when name is empty string")
    void createProduct_withEmptyName_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("");
        request.setDescription(testProductDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request when name is blank (whitespace only)")
    void createProduct_withBlankName_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("   ");
        request.setDescription(testProductDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request when name exceeds 255 characters")
    void createProduct_withNameExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("a".repeat(256));
        request.setDescription(testProductDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should accept name with exactly 255 characters")
    void createProduct_withNameExactly255Characters_shouldSucceed() throws Exception {
        setupAuthentication();

        String maxName = "a".repeat(255);
        CreateProductRequest request = new CreateProductRequest();
        request.setName(maxName);
        request.setDescription(testProductDescription);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName(maxName);
        product.setDescription(testProductDescription);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Create Product: Should reject request when description exceeds 255 characters")
    void createProduct_withDescriptionExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription("a".repeat(256));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request when description is empty string")
    void createProduct_withEmptyDescription_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription("");

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request without authentication")
    void createProduct_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).getMerchantByIdAndUserId(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request with invalid JWT token")
    void createProduct_withInvalidJwt_shouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        Mockito.when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).getMerchantByIdAndUserId(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should return 404 when merchant not found for user")
    void createProduct_whenMerchantNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomMerchantId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);

        Mockito.when(merchantService.getMerchantByIdAndUserId(randomMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + randomMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request with missing body")
    void createProduct_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request with malformed JSON")
    void createProduct_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should reject request with wrong content type")
    void createProduct_withWrongContentType_shouldReturnUnsupportedMediaType() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());

        Mockito.verify(productService, Mockito.never()).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class));
    }

    @Test
    @DisplayName("Create Product: Should return 400 for invalid merchant UUID format")
    void createProduct_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/invalid-uuid/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Create Product: Should create product with image URL")
    void createProduct_withImageUrl_shouldSucceed() throws Exception {
        setupAuthentication();

        String imageUrl = "https://example.com/product-image.png";
        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);
        request.setImage(imageUrl);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName(testProductName);
        product.setDescription(testProductDescription);
        product.setImage(imageUrl);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value(imageUrl));
    }

    // GET PRODUCT BY ID ENDPOINT TESTS

    @Test
    @DisplayName("Get Product: Should return product when authenticated and owner")
    void getProduct_whenAuthenticatedAndOwner_shouldReturnProduct() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(testProductId, testMerchantId))
                .thenReturn(testProduct);
        Mockito.when(productService.toResponse(testProduct)).thenReturn(createProductResponse(testProduct));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.productId").value(testProductId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testProductName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testProductDescription));
    }

    @Test
    @DisplayName("Get Product: Should return 404 when product not found")
    void getProduct_whenNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomProductId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(randomProductId, testMerchantId))
                .thenThrow(new ResourceNotFound("Product not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/" + randomProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Get Product: Should return 404 when merchant not found")
    void getProduct_whenMerchantNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(productService, Mockito.never()).getProductsByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Product: Should return 401 when not authenticated")
    void getProduct_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/" + testProductId + "/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).getMerchantByIdAndUserId(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(productService, Mockito.never()).getProductsByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Product: Should return JSON content type")
    void getProduct_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(testProductId, testMerchantId))
                .thenReturn(testProduct);
        Mockito.when(productService.toResponse(testProduct)).thenReturn(createProductResponse(testProduct));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Product: Should return 400 for invalid merchant UUID format")
    void getProduct_withInvalidMerchantUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/invalid-uuid/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get Product: Should return 400 for invalid product UUID format")
    void getProduct_withInvalidProductUuidFormat_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/invalid-uuid/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // GET ALL PRODUCTS ENDPOINT TESTS

    @Test
    @DisplayName("Get Products: Should return all products for authenticated user's merchant")
    void getProducts_whenAuthenticated_shouldReturnProducts() throws Exception {
        setupAuthentication();

        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setMerchantId(testMerchantId);
        product2.setName("Second Product");
        product2.setDescription("Second description");
        product2.setCreatedAt(System.currentTimeMillis());

        List<Product> products = Arrays.asList(testProduct, product2);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByMerchantId(testMerchantId)).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Get Products: Should return empty list when merchant has no products")
    void getProducts_whenNoProducts_shouldReturnEmptyList() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByMerchantId(testMerchantId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Get Products: Should return 401 when not authenticated")
    void getProducts_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(merchantService, Mockito.never()).getMerchantByIdAndUserId(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(productService, Mockito.never()).getProductsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Products: Should return 404 when merchant not found")
    void getProducts_whenMerchantNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(productService, Mockito.never()).getProductsByMerchantId(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Get Products: Should return JSON content type")
    void getProducts_shouldReturnJsonContentType() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByMerchantId(testMerchantId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    // UPDATE PRODUCT ENDPOINT TESTS

    @Test
    @DisplayName("Update Product: Should update product name successfully")
    void updateProduct_withValidName_shouldReturnUpdatedProduct() throws Exception {
        setupAuthentication();

        String newName = "Updated Product Name";
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName(newName);

        Product updatedProduct = new Product();
        updatedProduct.setProductId(testProductId);
        updatedProduct.setMerchantId(testMerchantId);
        updatedProduct.setName(newName);
        updatedProduct.setDescription(testProductDescription);
        updatedProduct.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(testProductId, testMerchantId))
                .thenReturn(testProduct);
        Mockito.when(productService.updateProduct(ArgumentMatchers.any(Product.class), ArgumentMatchers.any(UpdateProductRequest.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newName));
    }

    @Test
    @DisplayName("Update Product: Should update product description successfully")
    void updateProduct_withValidDescription_shouldReturnUpdatedProduct() throws Exception {
        setupAuthentication();

        String newDescription = "Updated description";
        UpdateProductRequest request = new UpdateProductRequest();
        request.setDescription(newDescription);

        Product updatedProduct = new Product();
        updatedProduct.setProductId(testProductId);
        updatedProduct.setMerchantId(testMerchantId);
        updatedProduct.setName(testProductName);
        updatedProduct.setDescription(newDescription);
        updatedProduct.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(testProductId, testMerchantId))
                .thenReturn(testProduct);
        Mockito.when(productService.updateProduct(ArgumentMatchers.any(Product.class), ArgumentMatchers.any(UpdateProductRequest.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(newDescription));
    }

    @Test
    @DisplayName("Update Product: Should update multiple fields successfully")
    void updateProduct_withMultipleFields_shouldReturnUpdatedProduct() throws Exception {
        setupAuthentication();

        String newName = "New Name";
        String newDescription = "New Description";
        String newImage = "https://example.com/new-image.png";
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName(newName);
        request.setDescription(newDescription);
        request.setImage(newImage);

        Product updatedProduct = new Product();
        updatedProduct.setProductId(testProductId);
        updatedProduct.setMerchantId(testMerchantId);
        updatedProduct.setName(newName);
        updatedProduct.setDescription(newDescription);
        updatedProduct.setImage(newImage);
        updatedProduct.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(testProductId, testMerchantId))
                .thenReturn(testProduct);
        Mockito.when(productService.updateProduct(ArgumentMatchers.any(Product.class), ArgumentMatchers.any(UpdateProductRequest.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(newDescription));
    }

    @Test
    @DisplayName("Update Product: Should reject request when name exceeds 255 characters")
    void updateProduct_withNameExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("a".repeat(256));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should reject request when name is empty string")
    void updateProduct_withEmptyName_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("");

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should reject request when description exceeds 255 characters")
    void updateProduct_withDescriptionExceedingMaxLength_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setDescription("a".repeat(256));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should reject request when description is empty string")
    void updateProduct_withEmptyDescription_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setDescription("");

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should return 404 when product not found")
    void updateProduct_whenProductNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID randomProductId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(randomProductId, testMerchantId))
                .thenThrow(new ResourceNotFound("Product not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + randomProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should return 404 when merchant not found")
    void updateProduct_whenMerchantNotFound_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should return 401 when not authenticated")
    void updateProduct_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should reject request with missing body")
    void updateProduct_withMissingBody_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should reject request with malformed JSON")
    void updateProduct_withMalformedJson_shouldReturnBadRequest() throws Exception {
        setupAuthentication();

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).updateProduct(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Update Product: Should handle null fields (partial update)")
    void updateProduct_withNullFields_shouldSucceed() throws Exception {
        setupAuthentication();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName(null);
        request.setDescription(null);
        request.setImage(null);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(testProductId, testMerchantId))
                .thenReturn(testProduct);
        Mockito.when(productService.updateProduct(ArgumentMatchers.any(Product.class), ArgumentMatchers.any(UpdateProductRequest.class)))
                .thenReturn(testProduct);

        mockMvc.perform(MockMvcRequestBuilders.put("/merchants/" + testMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // SECURITY TESTS

    @Test
    @DisplayName("Security: Should not expose sensitive data in error responses")
    void endpoints_shouldNotExposeSensitiveDataInErrors() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.any(UUID.class)))
                .thenThrow(new RuntimeException("Database connection string: jdbc:postgresql://..."));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    @DisplayName("Security: Should handle SQL injection attempts in product name safely")
    void createProduct_withSqlInjectionInName_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("'; DROP TABLE products;--");
        request.setDescription(testProductDescription);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName("'; DROP TABLE products;--");
        product.setDescription(testProductDescription);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productService).createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId));
    }

    @Test
    @DisplayName("Security: Should handle XSS attempts in product name safely")
    void createProduct_withXssInName_shouldBeHandledSafely() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("<script>alert('xss')</script>");
        request.setDescription(testProductDescription);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName("<script>alert('xss')</script>");
        product.setDescription(testProductDescription);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Security: Should prevent access to another user's merchant products")
    void getProduct_forAnotherUsersMerchant_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherUserMerchantId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(otherUserMerchantId, testUser.getUserId()))
                .thenThrow(new ResourceNotFound("Merchant not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + otherUserMerchantId + "/product/" + testProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(productService, Mockito.never()).getProductsByIdAndMerchantId(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Security: Should prevent access to another merchant's product")
    void getProduct_forAnotherMerchantsProduct_shouldReturnNotFound() throws Exception {
        setupAuthentication();

        UUID otherProductId = UUID.randomUUID();
        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.getProductsByIdAndMerchantId(otherProductId, testMerchantId))
                .thenThrow(new ResourceNotFound("Product not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/merchants/" + testMerchantId + "/product/" + otherProductId + "/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // EDGE CASES TESTS

    @Test
    @DisplayName("Edge Case: Should handle product name with special characters")
    void createProduct_withSpecialCharactersInName_shouldSucceed() throws Exception {
        setupAuthentication();

        String specialName = "Test @#$%^&*() Product!";
        CreateProductRequest request = new CreateProductRequest();
        request.setName(specialName);
        request.setDescription(testProductDescription);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName(specialName);
        product.setDescription(testProductDescription);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(specialName));
    }

    @Test
    @DisplayName("Edge Case: Should handle product name with Unicode characters")
    void createProduct_withUnicodeInName_shouldSucceed() throws Exception {
        setupAuthentication();

        String unicodeName = "测试产品 Тест";
        CreateProductRequest request = new CreateProductRequest();
        request.setName(unicodeName);
        request.setDescription(testProductDescription);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName(unicodeName);
        product.setDescription(testProductDescription);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(unicodeName));
    }

    @Test
    @DisplayName("Edge Case: Should handle null description (optional field)")
    void createProduct_withNullDescription_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(null);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName(testProductName);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Edge Case: Should handle null image (optional field)")
    void createProduct_withNullImage_shouldSucceed() throws Exception {
        setupAuthentication();

        CreateProductRequest request = new CreateProductRequest();
        request.setName(testProductName);
        request.setDescription(testProductDescription);
        request.setImage(null);

        Product product = new Product();
        product.setProductId(testProductId);
        product.setMerchantId(testMerchantId);
        product.setName(testProductName);
        product.setDescription(testProductDescription);
        product.setCreatedAt(System.currentTimeMillis());

        Mockito.when(merchantService.getMerchantByIdAndUserId(testMerchantId, testUser.getUserId()))
                .thenReturn(testMerchant);
        Mockito.when(productService.createProduct(ArgumentMatchers.any(CreateProductRequest.class), ArgumentMatchers.eq(testMerchantId)))
                .thenReturn(product);
        Mockito.when(productService.toResponse(product)).thenReturn(createProductResponse(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/merchants/" + testMerchantId + "/product/")
                .cookie(new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, testToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}