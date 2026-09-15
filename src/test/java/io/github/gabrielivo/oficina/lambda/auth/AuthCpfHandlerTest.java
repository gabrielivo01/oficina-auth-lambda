package io.github.gabrielivo.oficina.lambda.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.github.gabrielivo.oficina.lambda.auth.dto.ClienteStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCpfHandlerTest {

    private static final String CPF_VALIDO = "52998224725";
    private static final String SECRET = "01234567890123456789012345678901";

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    private final JwtIssuer jwtIssuer = new JwtIssuer(SECRET, 60_000L);

    private void stubLogger() {
        lenient().when(context.getLogger()).thenReturn(logger);
    }

    private APIGatewayProxyRequestEvent requestComCorpo(String body) {
        return new APIGatewayProxyRequestEvent().withBody(body);
    }

    @Test
    void deveRetornar400QuandoCpfInvalido() {
        AuthCpfHandler handler = new AuthCpfHandler((cpf) -> {
            throw new AssertionError("Não deveria consultar status para CPF inválido");
        }, jwtIssuer);

        APIGatewayProxyResponseEvent response = handler.handleRequest(
            requestComCorpo("{\"cpf\":\"12345678900\"}"), context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("erro"));
    }

    @Test
    void deveRetornar400QuandoCorpoAusente() {
        AuthCpfHandler handler = new AuthCpfHandler((cpf) -> {
            throw new AssertionError("Não deveria consultar status sem corpo");
        }, jwtIssuer);

        APIGatewayProxyResponseEvent response = handler.handleRequest(
            requestComCorpo(null), context);

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void deveRetornar401QuandoClienteNaoExiste() {
        AuthCpfHandler handler = new AuthCpfHandler(
            (cpf) -> new ClienteStatusResponse(false, false), jwtIssuer);

        APIGatewayProxyResponseEvent response = handler.handleRequest(
            requestComCorpo("{\"cpf\":\"" + CPF_VALIDO + "\"}"), context);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void deveRetornar401QuandoClienteInativo() {
        AuthCpfHandler handler = new AuthCpfHandler(
            (cpf) -> new ClienteStatusResponse(true, false), jwtIssuer);

        APIGatewayProxyResponseEvent response = handler.handleRequest(
            requestComCorpo("{\"cpf\":\"" + CPF_VALIDO + "\"}"), context);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    void deveRetornar200ComTokenQuandoClienteExisteEAtivo() {
        AuthCpfHandler handler = new AuthCpfHandler(
            (cpf) -> new ClienteStatusResponse(true, true), jwtIssuer);

        APIGatewayProxyResponseEvent response = handler.handleRequest(
            requestComCorpo("{\"cpf\":\"" + CPF_VALIDO + "\"}"), context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("token"));
    }

    @Test
    void deveRetornar500QuandoConsultaDeStatusFalha() {
        stubLogger();
        AuthCpfHandler handler = new AuthCpfHandler((cpf) -> {
            throw new RuntimeException("indisponível");
        }, jwtIssuer);

        APIGatewayProxyResponseEvent response = handler.handleRequest(
            requestComCorpo("{\"cpf\":\"" + CPF_VALIDO + "\"}"), context);

        assertEquals(500, response.getStatusCode());
    }
}
