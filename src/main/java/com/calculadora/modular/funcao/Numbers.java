package com.calculadora.modular.funcao;

import java.util.Locale;

// Ajuda a escrever número como gente escreve matemática: "−" de verdade (não hífen),
// vírgula decimal e zero sobrando fora. "3,0" não é coisa que se veja num caderno.
final class Numbers {

    private Numbers() {
    }

    // 3.0 -> "3", -1.5 -> "−1,5"
    static String format(double value) {
        if (value == 0) {
            return "0"; // evita o "−0", que é feio e não significa nada
        }
        // Formata sempre o valor positivo; o sinal a gente coloca à mão no final
        String text = String.format(Locale.ROOT, "%.2f", Math.abs(value));
        if (text.contains(".")) {
            // Tira os zeros do fim ("1.50" -> "1.5") e o ponto órfão ("3." -> "3")
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        text = text.replace('.', ',');
        return value < 0 ? "−" + text : text;
    }

    // Número negativo ganha parênteses: 3 -> "3", -3 -> "(−3)"
    static String wrap(double value) {
        return value < 0 ? "(" + format(value) + ")" : format(value);
    }

    // O sinal troca de lado: ("x", 3) -> "x − 3";  ("x", −3) -> "x + 3";  ("x", 0) -> "x"
    static String shifted(String variable, double h) {
        if (h == 0) {
            return variable;
        }
        return variable + (h > 0 ? " − " : " + ") + format(Math.abs(h));
    }

    // Termo somado no fim da expressão: " + 1", " − 2", ou nada quando k = 0
    static String addend(double k) {
        if (k == 0) {
            return "";
        }
        return (k > 0 ? " + " : " − ") + format(Math.abs(k));
    }

    // Escreve a reta m·x + b do jeito que se fala: (2, −5) -> "2x − 5";  (−1, 0) -> "−x";  (0, 3) -> "3"
    static String line(double m, double b) {
        // Inclinação 1 vira só "x" (ninguém escreve "1x"), e −1 vira "−x"
        String slope = m == 0 ? "" : m == 1 ? "x" : m == -1 ? "−x" : format(m) + "x";
        if (m == 0) {
            return format(b); // reta horizontal: sobra só o número
        }
        return slope + addend(b);
    }
}
