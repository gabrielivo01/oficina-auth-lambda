package io.github.gabrielivo.oficina.lambda.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gabrielivo.oficina.lambda.auth.dto.ClienteStatusResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Consulta o endpoint interno {@code GET /internal/clientes/{cpf}/status} da
 * aplicação principal, autenticando a chamada servico-a-servico com a mesma
 * chave configurada em {@code app.internal.api-key} (InternalApiKeyFilter).
 */
public class HttpClienteStatusPort implements ClienteStatusPort {

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpClienteStatusPort(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public static HttpClienteStatusPort fromEnvironment() {
        String baseUrl = System.getenv("INTERNAL_STATUS_BASE_URL");
        String apiKey = System.getenv("INTERNAL_API_KEY");
        return new HttpClienteStatusPort(baseUrl, apiKey);
    }

    @Override
    public ClienteStatusResponse consultar(String cpf) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/" + cpf + "/status"))
            .header("X-Internal-Api-Key", apiKey)
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                "Falha ao consultar status do cliente, status HTTP: " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), ClienteStatusResponse.class);
    }
}
