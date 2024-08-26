package com.finpro.roomio_backend.auth.service.impl;

import com.finpro.roomio_backend.auth.entity.UserAuth;
import com.finpro.roomio_backend.auth.entity.dto.LoginRequestDto;
import com.finpro.roomio_backend.auth.entity.dto.LoginResponseDto;
import com.finpro.roomio_backend.auth.repository.AuthRedisRepository;
import com.finpro.roomio_backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final AuthRedisRepository authRedisRepository;
  private final JwtEncoder jwtEncoder;


  @Override
  public String generateToken(Authentication authentication) {
    // for iat later
    Instant now = Instant.now();

    // define scope
    String scope = authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

    // Log the claims values
    log.debug("Authentication Name: {}", authentication.getName());
    log.debug("Scope: {}", scope);
    log.debug("Issued At: {}", now);
    log.debug("Expires At: {}", now.plus(12, ChronoUnit.HOURS));

    // jwt claims
    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(12, ChronoUnit.HOURS))
            .subject(authentication.getName())  // This is where it fails
            .claim("scope", scope)
            .build();

    // encode jwt
    var jwt = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

    // save in redis
    authRedisRepository.saveJwtKey(authentication.getName(), jwt);

    // return
    return jwt;
  }


  @Override
  public ResponseEntity<?> login(LoginRequestDto loginRequestDto) {
    try {
      // Authenticate user
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())
      );
      log.info("Authenticated user: {}", authentication);

      // Store it in the security context
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Get user's information
      UserAuth userDetails = (UserAuth) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      log.info("Principal: {}", userDetails);

      // Generate token
      String token = generateToken(authentication);
      log.info("Token generated successfully");

      // Generate response
      LoginResponseDto response = new LoginResponseDto();
      response.setMessage("Welcome, " + userDetails.getUsername() + "!");
      response.setToken(token);

      // Create response cookie
      ResponseCookie cookie = ResponseCookie.from("JSESSIONID", token)
              .path("/")
              .httpOnly(true)
              .maxAge(43200) // 12 hours
              .build();
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

      // Return the token
      return ResponseEntity.ok().headers(headers).body(response);
    } catch (BadCredentialsException ex) {
      // Handle bad credentials
      log.error("Authentication failed: Invalid username or password.", ex);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed. Invalid username or password.");
    } catch (LockedException ex) {
      // Handle locked account
      log.error("Account is locked.", ex);
      return ResponseEntity.status(HttpStatus.LOCKED).body("Account is locked.");
    } catch (IllegalArgumentException ex) {
      // Handle illegal argument exception (from generateToken)
      log.error("An error occurred while generating token.", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while generating token.");
    } catch (Exception ex) {
      // Handle other exceptions
      log.error("An internal error occurred.", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
    }
  }


  @Override
  public void logout() {
    // * Get logged in user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    String token = authRedisRepository.getJwtKey(username);

    if (token != null) {
      // * Invalidate token
      authRedisRepository.blacklistJwtKey(username);
    }
  }


}
