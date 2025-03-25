package site.caboomlog.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${token.secret}")
    private String secret;
    @Value("${token.expiration_time}")
    private Long expriationTime;

    public String generateToken(Long mbNo) {
        Claims claims = Jwts.claims().setSubject(mbNo.toString());

        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expriationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }



}
