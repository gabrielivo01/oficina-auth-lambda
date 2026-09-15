package io.github.gabrielivo.oficina.lambda.auth;

import io.github.gabrielivo.oficina.lambda.auth.dto.ClienteStatusResponse;

public interface ClienteStatusPort {
    ClienteStatusResponse consultar(String cpf) throws Exception;
}
