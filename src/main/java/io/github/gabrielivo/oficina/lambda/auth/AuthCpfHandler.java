package io.github.gabrielivo.oficina.lambda.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gabrielivo.oficina.lambda.auth.dto.ClienteStatusResponse;

import java.util.Map;

/**
 * Function serverless de autenticação por CPF: valida o formato do CPF,
 * consulta existência/status do cliente via endpoint interno da aplicação
 * principal e, se apto, emite um JWT aceito pelo {@code JwtAuthFilter} da
 * aplicação (ver JwtIssuer). Publicada atrás do API Gateway em
 * {@code POST /auth/cpf}.
 */
public class AuthCpfHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ClienteStatusPort clienteStatusPort;
    private final JwtIssuer jwtIssuer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthCpfHandler() {
        this(HttpClienteStatusPort.fromEnvironment(), JwtIssuer.fromEnvironment());
    }

    AuthCpfHandler(ClienteStatusPort clienteStatusPort, JwtIssuer jwtIssuer) {
        this.clienteStatusPort = clienteStatusPort;
        this.jwtIssuer = jwtIssuer;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String cpf = extrairCpf(event);

        if (!CpfValidator.isValid(cpf)) {
            return responder(400, Map.of("erro", "CPF inválido."));
        }

        String cpfNormalizado = CpfValidator.normalizar(cpf);

        try {
            ClienteStatusResponse status = clienteStatusPort.consultar(cpfNormalizado);

            if (!status.existe() || !status.ativo()) {
                return responder(401, Map.of("erro", "Cliente não encontrado ou inativo."));
            }

            String token = jwtIssuer.emitirParaCliente(cpfNormalizado);
            return responder(200, Map.of("token", token));
        } catch (Exception e) {
            context.getLogger().log("Erro ao autenticar cliente por CPF: " + e.getMessage());
            return responder(500, Map.of("erro", "Erro interno ao autenticar."));
        }
    }

    private String extrairCpf(APIGatewayProxyRequestEvent event) {
        try {
            String body = event.getBody();
            if (body == null || body.isBlank()) return null;
            return objectMapper.readValue(body, AuthCpfRequest.class).cpf();
        } catch (Exception e) {
            return null;
        }
    }

    private APIGatewayProxyResponseEvent responder(int statusCode, Map<String, String> body) {
        try {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"erro\":\"Erro interno ao autenticar.\"}");
        }
    }
}
