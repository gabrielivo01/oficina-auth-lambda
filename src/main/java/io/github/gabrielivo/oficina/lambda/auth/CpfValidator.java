package io.github.gabrielivo.oficina.lambda.auth;

/**
 * Mesma regra de validação de dígito verificador de CPF usada em
 * {@code shared/util/CpfValidator} na aplicação principal. Duplicada aqui
 * (em vez de compartilhada) porque este módulo é empacotado e implantado
 * de forma independente como uma AWS Lambda.
 */
public final class CpfValidator {

    private CpfValidator() {}

    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.isBlank()) return false;

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) return false;

        return validarDigitos(cpf);
    }

    private static boolean validarDigitos(String cpf) {
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;
        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;

        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    public static String normalizar(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }
}
