package com.calculadora.modular.funcao;

import java.util.ArrayList;
import java.util.List;

// A função modular do ensino médio: f(x) = a·|x − h| + k, com a ≠ 0.
// (Atenção: "modular" aqui é de módulo/valor absoluto, nada a ver com o mod da outra aba.)
//   a: abertura e sentido do "V" (a > 0 abre para cima, a < 0 para baixo)
//   h: deslocamento horizontal do vértice
//   k: deslocamento vertical do vértice
public record ModularFunction(double a, double h, double k) {

    public ModularFunction {
        // Com a = 0 o módulo some e sobra uma reta boba. Não aceitamos reta disfarçada de V.
        if (a == 0) {
            throw new IllegalArgumentException("O coeficiente a não pode ser 0, senão f deixa de ser modular.");
        }
    }

    public double value(double x) {
        return a * Math.abs(x - h) + k;
    }

    public boolean opensUp() {
        return a > 0;
    }

    // Conjunto imagem: o vértice é o menor valor (a > 0) ou o maior (a < 0), e a função vai até o infinito
    public String image() {
        return opensUp() ? "[" + Numbers.format(k) + ", +∞)" : "(−∞, " + Numbers.format(k) + "]";
    }

    // Soluções reais de f(x) = c: pode ter 0, 1 ou 2 (sempre em ordem crescente)
    public List<Double> solve(double c) {
        double r = (c - k) / a; // isolando o módulo sobra |x − h| = r
        List<Double> solutions = new ArrayList<>();
        if (r > 0) {
            solutions.add(h - r); // dois pontos, um de cada lado do vértice
            solutions.add(h + r);
        } else if (r == 0) {
            solutions.add(h);     // bem em cima do vértice
        }
        // r < 0: módulo nunca é negativo, lista vazia
        return solutions;
    }

    // Escreve a lei da função: "f(x) = 2|x − 3| + 1"
    public String expression() {
        String coefficient = a == 1 ? "" : a == -1 ? "−" : Numbers.format(a);
        return "f(x) = " + coefficient + "|" + Numbers.shifted("x", h) + "|" + Numbers.addend(k);
    }

    // Tira o módulo: uma reta para x ≥ h e outra (espelhada) para x < h
    public List<String> piecewise() {
        String right = Numbers.line(a, k - a * h) + ",  se x ≥ " + Numbers.format(h);
        String left = Numbers.line(-a, k + a * h) + ",  se x < " + Numbers.format(h);
        return List.of(right, left);
    }

    // O passo a passo de f(x) = c, escrito como o professor faria no quadro
    public List<String> solveSteps(double c) {
        List<String> steps = new ArrayList<>();
        String inner = Numbers.shifted("x", h);
        String coefficient = a == 1 ? "" : a == -1 ? "−" : Numbers.format(a);

        steps.add(coefficient + "|" + inner + "|" + Numbers.addend(k) + " = " + Numbers.format(c));

        // Passa o k para o outro lado e divide por a, deixando o módulo sozinho
        double r = (c - k) / a;
        steps.add("|" + inner + "| = (" + Numbers.format(c) + " − " + Numbers.wrap(k) + ") ÷ " + Numbers.wrap(a)
                + " = " + Numbers.format(r));

        if (r < 0) {
            steps.add("Um módulo nunca é negativo, então não há solução real.");
        } else if (r == 0) {
            steps.add(inner + " = 0   →   x = " + Numbers.format(h));
        } else {
            // |u| = r quer dizer u = r ou u = −r
            steps.add(inner + " = " + Numbers.format(r) + "   ou   " + inner + " = " + Numbers.format(-r));
            steps.add("x = " + Numbers.format(h - r) + "   ou   x = " + Numbers.format(h + r));
        }
        return steps;
    }

    // Resolve f(x) < c, f(x) ≤ c, f(x) > c ou f(x) ≥ c. É aqui que mora a parte chata.
    public InequalitySolution solveInequality(Relation relation, double c) {
        double r = (c - k) / a; // depois de isolar o módulo: |x − h| ? r
        // Dividir por a negativo inverte o sinal da desigualdade (olha ele de novo)
        Relation inner = a < 0 ? relation.flipped() : relation;
        boolean strict = inner.isStrict();
        double inf = Double.POSITIVE_INFINITY;

        // O conjunto solução vira uma lista de intervalos [início, fim] para o gráfico sombrear.
        // O texto "set" diz se as pontas são abertas ou fechadas.
        List<double[]> intervals = new ArrayList<>();
        String set;
        if (inner.isLess()) {
            // |x − h| < r  ->  h − r < x < h + r  (um intervalinho em volta do vértice)
            if (r < 0 || (r == 0 && strict)) {
                set = "∅ (nenhum x serve)";
            } else if (r == 0) {
                intervals.add(new double[] {h, h});
                set = "{" + Numbers.format(h) + "}"; // só o próprio vértice
            } else {
                intervals.add(new double[] {h - r, h + r});
                String open = strict ? "(" : "[";
                String close = strict ? ")" : "]";
                set = open + Numbers.format(h - r) + ", " + Numbers.format(h + r) + close;
            }
        } else {
            // |x − h| > r  ->  x < h − r  ou  x > h + r  (tudo, menos o intervalinho do meio)
            if (r < 0 || (r == 0 && !strict)) {
                intervals.add(new double[] {-inf, inf});
                set = "ℝ (todo x serve)"; // módulo é sempre ≥ 0, então qualquer x passa
            } else if (r == 0) {
                intervals.add(new double[] {-inf, h});
                intervals.add(new double[] {h, inf});
                set = "ℝ − {" + Numbers.format(h) + "}"; // todo mundo, menos o vértice
            } else {
                intervals.add(new double[] {-inf, h - r});
                intervals.add(new double[] {h + r, inf});
                String close = strict ? ")" : "]";
                String open = strict ? "(" : "[";
                set = "(−∞, " + Numbers.format(h - r) + close + " ∪ " + open + Numbers.format(h + r) + ", +∞)";
            }
        }
        return new InequalitySolution(set, inequalitySteps(relation, inner, c, r), intervals);
    }

    // Monta o passo a passo da inequação, explicando cada decisão (inclusive a inversão de sinal)
    private List<String> inequalitySteps(Relation relation, Relation inner, double c, double r) {
        List<String> steps = new ArrayList<>();
        String u = Numbers.shifted("x", h);
        String coefficient = a == 1 ? "" : a == -1 ? "−" : Numbers.format(a);

        steps.add(coefficient + "|" + u + "|" + Numbers.addend(k) + " " + relation.symbol() + " " + Numbers.format(c));

        String isolated = "|" + u + "| " + inner.symbol() + " (" + Numbers.format(c) + " − " + Numbers.wrap(k)
                + ") ÷ " + Numbers.wrap(a) + " = " + Numbers.format(r);
        steps.add(isolated);
        if (a < 0) {
            steps.add("Dividimos por um número negativo, então o sinal da desigualdade se inverte.");
        }

        String s = inner.symbol();
        if (inner.isLess()) {
            // Caso "menor": o módulo fica preso entre −r e r
            if (r < 0 || (r == 0 && inner.isStrict())) {
                steps.add("Um módulo nunca é menor que " + (r < 0 ? "um número negativo" : "0")
                        + ", então não há solução.");
            } else if (r == 0) {
                steps.add("O módulo só é ≤ 0 quando vale 0:  " + u + " = 0");
            } else {
                steps.add(Numbers.format(-r) + " " + s + " " + u + " " + s + " " + Numbers.format(r));
                steps.add(Numbers.format(h - r) + " " + s + " x " + s + " " + Numbers.format(h + r));
            }
        } else {
            // Caso "maior": o módulo escapa para fora de −r e r (daí o "ou")
            if (r < 0) {
                steps.add("Um módulo nunca é negativo, então vale para todo x real.");
            } else if (r == 0 && !inner.isStrict()) {
                steps.add("O módulo é sempre ≥ 0, então vale para todo x real.");
            } else if (r == 0) {
                steps.add("O módulo só não é maior que 0 quando vale 0, então x ≠ " + Numbers.format(h) + ".");
            } else {
                steps.add(u + " " + s + " " + Numbers.format(r) + "   ou   " + u + " " + inner.flipped().symbol()
                        + " " + Numbers.format(-r));
                steps.add("x " + s + " " + Numbers.format(h + r) + "   ou   x " + inner.flipped().symbol() + " "
                        + Numbers.format(h - r));
            }
        }
        return steps;
    }

    // O resultado de uma inequação: o conjunto em texto, o passo a passo e os intervalos para o gráfico pintar
    public record InequalitySolution(String set, List<String> steps, List<double[]> intervals) {
    }
}
