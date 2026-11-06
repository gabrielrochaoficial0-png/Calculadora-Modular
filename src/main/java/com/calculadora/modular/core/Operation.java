package com.calculadora.modular.core;

// As cinco operações que a calculadora sabe fazer. Se quiser adicionar uma sexta,
// é só criar o valor aqui e ensinar a conta ao ModularCalculator (ele reclama se esquecer).
public enum Operation {
    ADD("+", "a + b", true),
    SUBTRACT("−", "a − b", true),
    MULTIPLY("×", "a × b", true),
    POWER("^", "aᵇ", true),
    INVERSE("⁻¹", "a⁻¹", false);

    private final String symbol;       // aparece na expressão, ex.: "7 + 5"
    private final String buttonLabel;  // o que está escrito no botão
    private final boolean binary;      // precisa do b? O inverso é solitário e dispensa

    Operation(String symbol, String buttonLabel, boolean binary) {
        this.symbol = symbol;
        this.buttonLabel = buttonLabel;
        this.binary = binary;
    }

    public String symbol() {
        return symbol;
    }

    public String buttonLabel() {
        return buttonLabel;
    }

    public boolean requiresSecondOperand() {
        return binary;
    }
}
