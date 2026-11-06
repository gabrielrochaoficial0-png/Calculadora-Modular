package com.calculadora.modular.core;

import java.math.BigInteger;

// O cérebro da aritmética modular. Não sabe que existe uma janela, e é assim que a gente gosta.
public class ModularCalculator {

    public Calculation calculate(Operation operation, BigInteger a, BigInteger b, BigInteger m) {
        // Módulo zero ou negativo? Aqui não, obrigado.
        if (m.signum() <= 0) {
            throw new IllegalArgumentException("O módulo deve ser um inteiro positivo.");
        }

        // O mod() do BigInteger sempre devolve algo entre 0 e m-1, mesmo quando a conta dá negativa.
        // (O % do Java não faz isso, e é uma pegadinha clássica.)
        BigInteger result = switch (operation) {
            case ADD -> a.add(b).mod(m);
            case SUBTRACT -> a.subtract(b).mod(m);
            case MULTIPLY -> a.multiply(b).mod(m);
            case POWER -> power(a, b, m);
            case INVERSE -> inverse(a, m);
        };

        // Se a operação não usa b, joga fora o que o usuário digitou nele
        if (!operation.requiresSecondOperand()) {
            b = null;
        }
        return new Calculation(operation, a, b, m, result);
    }

    private BigInteger power(BigInteger base, BigInteger exponent, BigInteger m) {
        // Expoente negativo: a^(-n) = (a⁻¹)^n. Primeiro acha o inverso, depois eleva
        if (exponent.signum() < 0) {
            return inverse(base, m).modPow(exponent.negate(), m);
        }
        // modPow não calcula a^b inteiro e depois divide (seria o fim do mundo com números grandes),
        // ele vai reduzindo no caminho
        return base.modPow(exponent, m);
    }

    // O inverso só existe quando mdc(a, m) = 1. Se não existir, a gente avisa em vez de fingir.
    private BigInteger inverse(BigInteger value, BigInteger m) {
        if (!value.gcd(m).equals(BigInteger.ONE)) {
            throw new IllegalArgumentException(
                    value + " não tem inverso módulo " + m + ", pois mdc(" + value + ", " + m + ") ≠ 1.");
        }
        return value.modInverse(m);
    }
}
