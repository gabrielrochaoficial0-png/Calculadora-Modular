package com.calculadora.modular.funcao;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

// Soma de dois módulos: f(x) = a·|x − p| + b·|x − q| + k.
// O truque: entre os "bicos" (x = p e x = q) a função é uma reta comum.
// Então a gente fatia o eixo x em trechos e resolve cada pedaço como se fosse coisa fácil.
public record ModularSum(double a, double p, double b, double q, double k) {

    // Um trecho em que f é a reta slope·x + intercept
    public record Piece(double from, double to, double slope, double intercept) {

        public boolean contains(double x) {
            return x >= from && x <= to;
        }

        // Escreve onde o trecho vale: "x < −2", "−2 ≤ x < 3" ou "x ≥ 3"
        public String domain() {
            if (from == Double.NEGATIVE_INFINITY) {
                return "x < " + Numbers.format(to);
            }
            if (to == Double.POSITIVE_INFINITY) {
                return "x ≥ " + Numbers.format(from);
            }
            return Numbers.format(from) + " ≤ x < " + Numbers.format(to);
        }
    }

    // Soluções soltas (pontos) e/ou trechos inteiros (quando f é constante e igual a c, aí é festa: todo x serve)
    public record Solution(List<Double> points, List<double[]> segments) {
    }

    public double value(double x) {
        return a * Math.abs(x - p) + b * Math.abs(x - q) + k;
    }

    // Os valores de x onde a função "quebra", em ordem e sem repetição.
    // O TreeSet faz o trabalho sujo: se p = q, os dois viram um só.
    public List<Double> breakpoints() {
        return new ArrayList<>(new TreeSet<>(List.of(p, q)));
    }

    // Fatia o eixo x nos bicos e descobre a reta de cada pedaço
    public List<Piece> pieces() {
        List<Double> points = breakpoints();
        List<Piece> pieces = new ArrayList<>();
        double start = Double.NEGATIVE_INFINITY;
        for (int i = 0; i <= points.size(); i++) {
            double end = i < points.size() ? points.get(i) : Double.POSITIVE_INFINITY;
            // Dentro do trecho f é uma reta, e dois pontos bastam para descobrir qual:
            // escolhemos dois bem no miolo (nunca em cima de um bico, que é onde a função se confunde).
            double first;
            double second;
            if (start == Double.NEGATIVE_INFINITY) {
                first = end - 2;
                second = end - 1;
            } else if (end == Double.POSITIVE_INFINITY) {
                first = start + 1;
                second = start + 2;
            } else {
                first = start + (end - start) / 3;
                second = start + 2 * (end - start) / 3;
            }
            // Ensino fundamental de volta: inclinação = variação de y ÷ variação de x
            double slope = (value(second) - value(first)) / (second - first);
            pieces.add(new Piece(start, end, slope, value(first) - slope * first));
            start = end;
        }
        return pieces;
    }

    // Conjunto imagem: depende de para onde o gráfico vai nas pontas
    public String image() {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        // Extremos só podem estar nos bicos (ou no infinito), então olhamos só neles
        for (double x : breakpoints()) {
            min = Math.min(min, value(x));
            max = Math.max(max, value(x));
        }
        double tail = a + b; // inclinação da ponta direita (a esquerda é a mesma com o sinal trocado)
        if (tail > 0) {
            return "[" + Numbers.format(min) + ", +∞)";  // sobe nas duas pontas
        }
        if (tail < 0) {
            return "(−∞, " + Numbers.format(max) + "]";  // desce nas duas pontas
        }
        // a + b = 0: as pontas são planas, então a função fica limitada entre o menor e o maior valor
        return min == max ? "{" + Numbers.format(min) + "}" : "[" + Numbers.format(min) + ", " + Numbers.format(max) + "]";
    }

    // Resolve f(x) = c em cada trecho e junta tudo
    public Solution solve(double c) {
        TreeSet<Double> points = new TreeSet<>();
        List<double[]> segments = new ArrayList<>();
        for (Piece piece : pieces()) {
            // Trecho horizontal: ou nunca bate em c, ou bate em todos os pontos de uma vez
            // (comparamos com uma folguinha, porque double e igualdade exata não se dão bem)
            if (Math.abs(piece.slope()) < 1e-9) {
                if (Math.abs(piece.intercept() - c) < 1e-9) {
                    segments.add(new double[] {piece.from(), piece.to()});
                }
                continue;
            }
            // Trecho inclinado: resolve a reta e só aceita se a resposta caiu dentro do próprio trecho
            double x = (c - piece.intercept()) / piece.slope();
            if (piece.contains(x)) {
                points.add(Math.round(x * 1e9) / 1e9); // arredonda a poeira de ponto flutuante
            }
        }
        // Se um ponto já está dentro de um trecho inteiro, listar ele de novo seria repetição
        points.removeIf(x -> segments.stream().anyMatch(s -> x >= s[0] && x <= s[1]));
        return new Solution(new ArrayList<>(points), segments);
    }

    // Escreve a lei da função: "f(x) = 2|x − 1| − |x + 2| + 3"
    public String expression() {
        StringBuilder text = new StringBuilder("f(x) = ");
        text.append(a < 0 ? "−" : "").append(coefficient(a)).append("|").append(Numbers.shifted("x", p)).append("|");
        text.append(b < 0 ? " − " : " + ").append(coefficient(b)).append("|").append(Numbers.shifted("x", q)).append("|");
        text.append(Numbers.addend(k));
        return text.toString();
    }

    // O coeficiente sem sinal (o sinal é tratado fora), e o 1 some porque ninguém escreve "1|x|"
    private static String coefficient(double value) {
        double abs = Math.abs(value);
        return abs == 1 ? "" : Numbers.format(abs);
    }

    // A função sem módulos: uma reta para cada trecho
    public List<String> piecewise() {
        List<String> lines = new ArrayList<>();
        for (Piece piece : pieces()) {
            lines.add(Numbers.line(piece.slope(), piece.intercept()) + ",  se " + piece.domain());
        }
        return lines;
    }

    // Passo a passo: resolve f(x) = c em cada trecho e confere se a resposta cai dentro dele
    // (✓ passa, ✗ é uma solução "de mentira" que só existe se esticarmos a reta para fora do trecho)
    public List<String> solveSteps(double c) {
        List<String> steps = new ArrayList<>();
        for (Piece piece : pieces()) {
            String equation = Numbers.line(piece.slope(), piece.intercept()) + " = " + Numbers.format(c);
            String prefix = "Se " + piece.domain() + ":  " + equation;
            if (Math.abs(piece.slope()) < 1e-9) {
                boolean all = Math.abs(piece.intercept() - c) < 1e-9;
                steps.add(prefix + (all ? "   →   vale para todo x do trecho" : "   →   impossível"));
                continue;
            }
            double x = (c - piece.intercept()) / piece.slope();
            boolean inside = piece.contains(x);
            steps.add(prefix + "   →   x = " + Numbers.format(x) + (inside ? "  ✓" : "  ✗ (fora do trecho)"));
        }
        return steps;
    }
}
