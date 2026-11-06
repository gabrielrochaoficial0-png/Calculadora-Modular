package com.calculadora.modular.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// Confere se as contas modulares dão o que têm que dar (incluindo os casos que costumam dar dor de cabeça)
class ModularCalculatorTest {

    private final ModularCalculator calculator = new ModularCalculator();

    // Uma tabelinha de contas conhecidas. Repare nos negativos: o resultado nunca sai de [0, m)
    @ParameterizedTest(name = "{1} {0} {2} mod {3} = {4}")
    @CsvSource({
            "ADD,       7,   5,  5,  2",
            "ADD,      -3,   1,  7,  5",
            "SUBTRACT,  2,   9,  7,  0",
            "SUBTRACT,  1,   4,  7,  4",
            "MULTIPLY,  6,   7, 10,  2",
            "MULTIPLY, -2,   3,  7,  1",
            "POWER,     3, 200, 13,  9",
            "POWER,     5,   0,  7,  1",
            "POWER,     3,  -1, 11,  4",
    })
    void computesBinaryOperations(Operation operation, long a, long b, long m, long expected) {
        assertEquals(big(expected), calculate(operation, big(a), big(b), big(m)));
    }

    @Test
    void computesModularInverse() {
        assertEquals(big(4), calculate(Operation.INVERSE, big(3), null, big(11)));
    }

    @Test
    void inverseOfNegativeValueIsNormalized() {
        assertEquals(big(7), calculate(Operation.INVERSE, big(-3), null, big(11)));
    }

    // Números tão grandes que um long nem sonha em guardar. Se o BigInteger falhar, a gente descobre aqui
    @Test
    void handlesNumbersBeyondLongRange() {
        BigInteger mersenne = BigInteger.TWO.pow(127).subtract(BigInteger.ONE);
        BigInteger huge = BigInteger.TEN.pow(60);

        BigInteger result = calculate(Operation.POWER, huge, mersenne.subtract(BigInteger.ONE), mersenne);

        assertEquals(BigInteger.ONE, result, "Pequeno teorema de Fermat");
    }

    // 4 e 10 dividem por 2, então não existe inverso. A calculadora tem que admitir isso com educação
    @Test
    void rejectsInverseWhenNotCoprime() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> calculate(Operation.INVERSE, big(4), null, big(10)));
        assertEquals("4 não tem inverso módulo 10, pois mdc(4, 10) ≠ 1.", error.getMessage());
    }

    @Test
    void rejectsNegativeExponentWhenBaseHasNoInverse() {
        assertThrows(IllegalArgumentException.class,
                () -> calculate(Operation.POWER, big(2), big(-1), big(8)));
    }

    @ParameterizedTest
    @CsvSource({"0", "-5"})
    void rejectsNonPositiveModulus(long modulus) {
        assertThrows(IllegalArgumentException.class,
                () -> calculate(Operation.ADD, big(1), big(1), big(modulus)));
    }

    @Test
    void describesCalculationAsCongruence() {
        Calculation binary = calculator.calculate(Operation.ADD, big(7), big(5), big(5));
        Calculation unary = calculator.calculate(Operation.INVERSE, big(3), big(99), big(11));

        assertEquals("7 + 5 ≡ 2 (mod 5)", binary.expression());
        assertEquals("3⁻¹ ≡ 4 (mod 11)", unary.expression());
    }

    // Atalhos para os testes não ficarem poluídos de BigInteger.valueOf(...)
    private BigInteger calculate(Operation operation, BigInteger a, BigInteger b, BigInteger m) {
        return calculator.calculate(operation, a, b, m).result();
    }

    private static BigInteger big(long value) {
        return BigInteger.valueOf(value);
    }
}
