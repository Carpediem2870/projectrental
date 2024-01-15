package com.team5.projrental.common.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team5.projrental.common.Const;
import com.team5.projrental.common.SecurityProperties;
import com.team5.projrental.common.security.model.SecurityPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;
    private SecretKeySpec spec;

    @PostConstruct
    public void init() {
        this.spec = new SecretKeySpec(properties.getJwt().getSecret().getBytes(),
                SignatureAlgorithm.HS256.getJcaName());
    }


    public String generateAccessToken(SecurityPrincipal principal) {
        return generateToken(principal, properties.getJwt().getAccessTokenExpiry());
    }

    public String generateRefreshToken(SecurityPrincipal principal) {
        return generateToken(principal, properties.getJwt().getRefreshTokenExpiry());
    }



    public String getTokenFromHeader(HttpServletRequest request) {
        String authz = request.getHeader(properties.getJwt().getHeaderSchemeName());
        return authz == null ? null : authz.startsWith(properties.getJwt().getTokenType()) ?
                authz.substring(properties.getJwt().getTokenType().length()).trim() : null;
    }

    public boolean isValidatedToken(String token) {
        return !getAllClaims(token).getExpiration().before(new Date());
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = getUserDetailsFromToken(token);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public UserDetails getUserDetailsFromToken(String token) {
        Claims claims = getAllClaims(token);
        String json = (String) claims.get("user");
        SecurityPrincipal principal;
        try {
            principal = objectMapper.readValue(json, SecurityPrincipal.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Const.SERVER_ERR_MESSAGE);
        }
        return new SecurityUserDetails(principal);
    }

    private String generateToken(SecurityPrincipal principal, Long tokenValidMs) {
        return Jwts.builder()
                .claims(createClaims(principal))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenValidMs))
                .signWith(spec)
                .compact();

    }

    private Claims createClaims(SecurityPrincipal principal) {
        String json;

        try {
            json = objectMapper.writeValueAsString(principal);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Const.SERVER_ERR_MESSAGE);
        }
        return Jwts.claims()
                .add("user", json)
                .build();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(spec)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}