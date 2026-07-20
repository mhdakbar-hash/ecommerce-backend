package com.akbar.ecommerce_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;


    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }


    public String generateToken(UserDetails userDetails){

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();

    }



    public String extractUserName(String token){

        return extractClaim(token, Claims::getSubject);

    }


    private Claims extractAllClaims(String token){

        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }


    public Date extractExpiration(String token){

        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }


    public boolean isTokenValid(String token, UserDetails userDetails){

        String username = extractUserName(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

}
