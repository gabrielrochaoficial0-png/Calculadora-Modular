package com.calculadora.modular.core;

import java.math.BigInteger;

// Um cálculo já feito, congelado para a posteridade (e para o histórico).
// No inverso o secondOperand é null, porque ele não tem b e não vai inventar um.
public record Calculation(Operation operation, BigInteger firstOperand, BigInteger secondOperand,
                          BigInteger modulus, BigInteger result) {

    // Monta o texto bonito, tipo "7 + 5 ≡ 2 (mod 5)" ou "3⁻¹ ≡ 4 (mod 11)"
    public String expression() {
        // Começa com o formato do inverso (colado, sem espaços)...
        String left = firstOperand + operation.symbol();
        // ...e se tiver b, troca pelo formato de conta normal
        if (operation.requiresSecondOperand()) {
            left = firstOperand + " " + operation.symbol() + " " + secondOperand;
        }
        return left + " ≡ " + result + " (mod " + modulus + ")";
    }
}
