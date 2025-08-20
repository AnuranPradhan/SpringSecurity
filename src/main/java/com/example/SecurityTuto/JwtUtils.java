package com.example.SecurityTuto;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;
//Getting the token from the Authorization Header
    public String getJwtFromHeader(HttpServletRequest request) {
        String BearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", BearerToken);
        if(BearerToken!=null && BearerToken.startsWith("Bearer ")){
            return BearerToken.substring(7);
        }
        return null;
    }

//Getting token from the username
    public String generateTokenUsername(UserDetails userDetails){
        String username=userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
                .signWith(key())
                .compact();

    }
//Getting the username from the token
 public String getUserNameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
 }
//Generating the Signing Key
    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }
  //Validate JWT Token
  public boolean validateJwtToken(String token){
        try{
            System.out.println("Validate");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token);
            return true;

        }catch(MalformedJwtException e){
          logger.error("Invalid JWT token: {}", e.getMessage());
        } catch(ExpiredJwtException e){
         logger.error("Expired JWT token: {}", e.getMessage());
        } catch(UnsupportedJwtException e){
logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch(IllegalArgumentException e){
logger.error("JWT claims string is empty: {}", e.getMessage());
        }
return false;
  }
}
