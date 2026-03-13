package com.zenz.crypto_payment_gateway.api.route.auth;

import com.zenz.crypto_payment_gateway.api.route.auth.model.request.LoginRequest;
import com.zenz.crypto_payment_gateway.api.route.auth.model.request.RegisterRequest;
import com.zenz.crypto_payment_gateway.api.route.auth.model.response.MeResponse;
import com.zenz.crypto_payment_gateway.api.config.JWTAuthenticationFilter;
import com.zenz.crypto_payment_gateway.entity.User;
import com.zenz.crypto_payment_gateway.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register/")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest body, HttpServletResponse response) {
        User user = authService.createUser(body.getEmail(), body.getPassword());
        String token = authService.login(body.getEmail(), body.getPassword());
        setJwtCookie(response, token);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/login/")
    public ResponseEntity<Void> login(@RequestBody LoginRequest body, HttpServletResponse response) {
        String token = authService.login(body.getEmail(), body.getPassword());
        setJwtCookie(response, token);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/me/")
    public ResponseEntity<MeResponse> getMe(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        MeResponse response = new MeResponse();
        response.setEmail(user.getEmail());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout/")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Clear cookie
        response.addCookie(cookie);
        
        return ResponseEntity.ok().build();
    }
    
    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWTAuthenticationFilter.JWT_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(100_000 * 60 * 60);
        response.addCookie(cookie);
    }
}