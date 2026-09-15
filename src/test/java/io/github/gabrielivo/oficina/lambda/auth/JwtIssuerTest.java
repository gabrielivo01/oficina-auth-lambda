package io.github.gabrielivo.oficina.lambda.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtIssuerTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void deveEmitirTokenComSubjectCpfETipoCliente() {
        JwtIssuer jwtIssuer = new JwtIssuer(SECRET, 60_000L);

        String token = jwtIssuer.emitirParaCliente("52998224725");

        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();

        assertEquals("52998224725", claims.getSubject());
        assertEquals("CLIENTE", claims.get("tipo", String.class));
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }
}
