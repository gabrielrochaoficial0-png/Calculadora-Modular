package com.calculadora.modular.funcao;

// O sinal de uma inequação: f(x) < c, f(x) ≤ c, f(x) > c ou f(x) ≥ c.
// (A igualdade não está aqui: ela ganhou tratamento VIP e é representada por "null" na tela.)
public enum Relation {
    LESS("<"),
    LESS_OR_EQUAL("≤"),
    GREATER(">"),
    GREATER_OR_EQUAL("≥");

    private final String symbol;

    Relation(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    // "Menor" (< ou ≤) ou "maior" (> ou ≥)?
    public boolean isLess() {
        return this == LESS || this == LESS_OR_EQUAL;
    }

    // Estrito = não aceita a igualdade (< e >), então as pontas do intervalo ficam abertas
    public boolean isStrict() {
        return this == LESS || this == GREATER;
    }

    // O famoso "dividiu por negativo, inverte o sinal". Clássico que derruba prova até hoje
    public Relation flipped() {
        return switch (this) {
            case LESS -> GREATER;
            case LESS_OR_EQUAL -> GREATER_OR_EQUAL;
            case GREATER -> LESS;
            case GREATER_OR_EQUAL -> LESS_OR_EQUAL;
        };
    }
}
