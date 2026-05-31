package br.com.fiap.user.infrastructure.outbound.security;

import br.com.fiap.user.application.domain.user.User;
import br.com.fiap.user.application.ports.outbound.token.TokenGeneratorPort;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component
public class JwtTokenAdapter implements TokenGeneratorPort {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.expiration}")
    private String expiration;

    @Override
    public String generate(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("restaurant-api")
                    .withSubject(user.getLogin())
                    .withClaim("roles", user.getRoles().stream().map(r -> r.name()).toList())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error generating token", exception);
        }
    }

    @Override
    public String getSubjectByToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("restaurant-api")
                .build()
                .verify(token)
                .getSubject();
    }

    @Override
    public Instant genExpirationDate() {
        long seconds = Long.parseLong(expiration);
        return LocalDateTime.now().plusSeconds(seconds).toInstant(ZoneOffset.of("-03:00"));
    }

    @Override
    public boolean isExpiredToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);

        Date dateExpired = decodedJWT.getExpiresAt();

        return dateExpired.before(new Date());
    }
}
