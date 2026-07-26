package com.valhora.backend.auth;

import com.valhora.backend.auth.dto.AuthResponse;
import com.valhora.backend.auth.dto.LoginRequest;
import com.valhora.backend.auth.dto.RegisterRequest;
import com.valhora.backend.common.exception.DuplicateResourceException;
import com.valhora.backend.users.Role;
import com.valhora.backend.users.User;
import com.valhora.backend.users.UserMapper;
import com.valhora.backend.users.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe una cuenta con ese email");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(user);

        return issueSession(user);
    }

    public AuthResult login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email o contraseña incorrectos"));

        return issueSession(user);
    }

    public AuthResult refresh(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.validate(rawRefreshToken);
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Sesión inválida"));

        refreshTokenService.revoke(refreshToken);
        return issueSession(user);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeByRawToken(rawRefreshToken);
    }

    private AuthResult issueSession(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user.getId());
        AuthResponse response = new AuthResponse(accessToken, userMapper.toResponse(user));
        return new AuthResult(response, refreshToken);
    }
}
