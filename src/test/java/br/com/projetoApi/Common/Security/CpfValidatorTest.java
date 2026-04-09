package br.com.projetoApi.Common.Security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfValidatorTest {

    @Test
    void shouldAcceptValidCpf() {
        assertTrue(CpfValidator.isValid("52998224725"));
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertFalse(CpfValidator.isValid("11111111111"));
        assertFalse(CpfValidator.isValid("123"));
    }
}
