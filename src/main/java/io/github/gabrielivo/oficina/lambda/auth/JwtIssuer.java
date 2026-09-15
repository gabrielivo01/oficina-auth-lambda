package io.github.gabrielivo.oficina.lambda.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Emite tokens compatíveis com {@code infrastructure/security/JwtService} da
 * aplicação principal: mesmo algoritmo (HS256) e mesmo segredo (compartilhado
 * via AWS Secrets Manager), com {@code sub}=CPF e a claim {@code tipo=CLIENTE}
 * que {@code JwtAuthFilter} usa para rotear a autenticação para
 * {@code ClienteUserDetailsService} em vez do fluxo de usuário/senha.
 */
public class JwtIssuer {

    private final Key key;
    private final long expirationMs;

    public JwtIssuer(String secret, long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public static JwtIssuer fromEnvironment() {
        String secret = System.getenv("JWT_SECRET");
        String expiration = System.getenv("JWT_EXPIRATION_MS");
        long expirationMs = expiration != null ? Long.parseLong(expiration) : 1_800_000L;
        return new JwtIssuer(secret, expirationMs);
    }

    public String emitirParaCliente(String cpf) {
        Date agora = new Date();
        return Jwts.builder()
            .setSubject(cpf)
            .claim("tipo", "CLIENTE")
            .setIssuedAt(agora)
            .setExpiration(new Date(agora.getTime() + expirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }
}
