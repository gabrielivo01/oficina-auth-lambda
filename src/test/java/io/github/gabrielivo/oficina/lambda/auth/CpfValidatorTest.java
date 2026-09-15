package io.github.gabrielivo.oficina.lambda.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @Test
    void deveAceitarCpfValido() {
        assertTrue(CpfValidator.isValid("52998224725"));
    }

    @Test
    void deveAceitarCpfValidoComMascara() {
        assertTrue(CpfValidator.isValid("529.982.247-25"));
    }

    @Test
    void deveRejeitarCpfNuloOuVazio() {
        assertFalse(CpfValidator.isValid(null));
        assertFalse(CpfValidator.isValid(""));
        assertFalse(CpfValidator.isValid("   "));
    }

    @Test
    void deveRejeitarCpfComDigitosRepetidos() {
        assertFalse(CpfValidator.isValid("11111111111"));
    }

    @Test
    void deveRejeitarCpfComTamanhoErrado() {
        assertFalse(CpfValidator.isValid("123456789"));
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorInvalido() {
        assertFalse(CpfValidator.isValid("52998224700"));
    }

    @Test
    void deveNormalizarRemovendoMascara() {
        assertEquals("52998224725", CpfValidator.normalizar("529.982.247-25"));
    }
}
