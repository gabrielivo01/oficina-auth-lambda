package io.github.gabrielivo.oficina.lambda.auth.dto;

/**
 * Espelha o corpo retornado por {@code GET /internal/clientes/{cpf}/status}
 * na aplicação principal (presentation/cliente/ClienteStatusResponse).
 */
public record ClienteStatusResponse(boolean existe, boolean ativo) {}
