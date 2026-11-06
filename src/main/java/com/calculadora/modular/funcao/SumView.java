package com.calculadora.modular.funcao;

import static com.calculadora.modular.funcao.FunctionView.CURVE;
import static com.calculadora.modular.funcao.FunctionView.EQUATION;
import static com.calculadora.modular.funcao.FunctionView.POINT;
import static com.calculadora.modular.funcao.FunctionView.RANGE;

import com.calculadora.modular.funcao.FunctionView.Plane;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

// A tela da soma de módulos f(x) = a·|x − p| + b·|x − q| + k, resolvida trecho a trecho.
// Parente próxima da FunctionView: copiamos o estilo e emprestamos as ferramentas de desenho dela.
public class SumView {

    // Os controles. Os valores iniciais foram escolhidos para dar um gráfico bonito logo de cara
    private final DoubleProperty a = new SimpleDoubleProperty(1);
    private final DoubleProperty p = new SimpleDoubleProperty(-2);
    private final DoubleProperty b = new SimpleDoubleProperty(1);
    private final DoubleProperty q = new SimpleDoubleProperty(3);
    private final DoubleProperty k = new SimpleDoubleProperty(0);
    private final DoubleProperty c = new SimpleDoubleProperty(7);
    private final DoubleProperty x0 = new SimpleDoubleProperty(0);

    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas();
    private final Label expression = new Label();
    private final Label details = new Label();

    public SumView() {
        root.getStyleClass().add("root-pane");
        root.setTop(header());
        root.setLeft(controlsCard());
        root.setCenter(graphCard());

        // Mexeu em qualquer slider? Refaz tudo
        for (DoubleProperty property : List.of(a, p, b, q, k, c, x0)) {
            property.addListener(obs -> refresh());
        }
        refresh();
    }

    public Parent root() {
        return root;
    }

    private Region header() {
        Label title = new Label("Soma de Módulos");
        title.getStyleClass().add("title");
        Label subtitle = new Label("f(x) = a·|x − p| + b·|x − q| + k   —   a função é uma reta diferente entre cada \"bico\"");
        subtitle.getStyleClass().add("subtitle");
        VBox header = new VBox(2, title, subtitle);
        header.getStyleClass().add("header");
        return header;
    }

    private Region controlsCard() {
        expression.getStyleClass().add("function-expression");
        expression.setWrapText(true);
        // Fonte menor que a da outra aba: a expressão aqui é bem mais comprida e precisa caber
        expression.setStyle("-fx-font-size: 18px;");
        VBox display = new VBox(6, expression);
        display.getStyleClass().add("result-display");

        VBox card = new VBox(10,
                display,
                FunctionView.slider("a", a, -5, 5),
                FunctionView.slider("p  (primeiro bico)", p, -8, 8),
                FunctionView.slider("b", b, -5, 5),
                FunctionView.slider("q  (segundo bico)", q, -8, 8),
                FunctionView.slider("k  (desloca na vertical)", k, -8, 8),
                FunctionView.slider("c  (resolver f(x) = c)", c, -10, 10),
                FunctionView.slider("x  (calcular f(x))", x0, -10, 10));
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        BorderPane.setMargin(card, new Insets(0, 16, 0, 0));
        return card;
    }

    private Region graphCard() {
        // O canvas se estica junto com o cartão e redesenha quando o tamanho muda
        Pane graph = new Pane(canvas);
        canvas.widthProperty().bind(graph.widthProperty());
        canvas.heightProperty().bind(graph.heightProperty());
        canvas.widthProperty().addListener(obs -> draw());
        canvas.heightProperty().addListener(obs -> draw());

        // Aqui o texto de baixo é longo (um passo por trecho), então ganha uma barra de rolagem
        details.setWrapText(true);
        details.getStyleClass().add("result-expression");
        ScrollPane scroll = new ScrollPane(details);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(190);
        scroll.setMinHeight(190);
        // Tira o fundo cinza padrão do ScrollPane para ele se misturar com o cartão
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox card = new VBox(12, graph, scroll);
        VBox.setVgrow(graph, Priority.ALWAYS);
        card.getStyleClass().add("card");
        return card;
    }

    private ModularSum function() {
        // Aqui a ou b iguais a 0 são permitidos: só "desligam" um dos módulos, e tá tudo bem
        return new ModularSum(a.get(), p.get(), b.get(), q.get(), k.get());
    }

    private void refresh() {
        ModularSum f = function();
        expression.setText(f.expression());
        details.setText(describe(f));
        draw();
    }

    // Monta o texto de baixo: bicos, imagem, função por partes, valor em x e a resolução
    private String describe(ModularSum f) {
        StringBuilder text = new StringBuilder();
        text.append("Bicos em x = ");
        List<String> points = new ArrayList<>();
        for (double x : f.breakpoints()) {
            points.add(Numbers.format(x) + " (f = " + Numbers.format(f.value(x)) + ")");
        }
        text.append(String.join("  e  x = ", points)).append('\n');
        text.append("Domínio: ℝ      Imagem: ").append(f.image()).append("\n\n");

        text.append("Sem os módulos:\n");
        for (String line : f.piecewise()) {
            text.append("   ").append(line).append('\n');
        }

        double x = x0.get();
        text.append("\nValor em x = ").append(Numbers.format(x)).append(":  f(").append(Numbers.format(x)).append(") = ")
                .append(Numbers.format(f.value(x))).append("\n\n");

        text.append("Resolvendo f(x) = ").append(Numbers.format(c.get())).append(", trecho a trecho:\n");
        for (String step : f.solveSteps(c.get())) {
            text.append("   ").append(step).append('\n');
        }

        // Junta pontos soltos e trechos inteiros numa lista só para escrever o conjunto solução S
        ModularSum.Solution solution = f.solve(c.get());
        List<String> found = new ArrayList<>();
        solution.points().forEach(x1 -> found.add(Numbers.format(x1)));
        solution.segments().forEach(s -> found.add("todo x em [" + bound(s[0]) + ", " + bound(s[1]) + "]"));
        text.append("   S = ").append(found.isEmpty() ? "∅ (nenhum x serve)" : "{ " + String.join(" ; ", found) + " }");
        return text.toString();
    }

    // Escreve o limite de um intervalo, com o infinito em dia
    private static String bound(double value) {
        if (value == Double.NEGATIVE_INFINITY) {
            return "−∞";
        }
        return value == Double.POSITIVE_INFINITY ? "+∞" : Numbers.format(value);
    }

    // ---- desenho do gráfico ----

    private void draw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        // Canvas de tamanho zero (janela ainda abrindo)? Não tem onde desenhar
        if (width <= 0 || height <= 0) {
            return;
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, width, height);

        // Mesma regra da outra aba: escala pelo menor lado para o gráfico não ficar esticado
        double scale = Math.min(width, height) / (2 * RANGE);
        Plane plane = new Plane(width / 2, height / 2, scale);
        FunctionView.drawGrid(g, plane, width, height);

        ModularSum f = function();
        // O que é desenhado depois fica por cima do que veio antes
        drawEquationLine(g, plane, f, width);
        drawCurve(g, plane, f);
        for (double x : f.breakpoints()) {
            FunctionView.drawPoint(g, plane, x, f.value(x), POINT,
                    "(" + Numbers.format(x) + ", " + Numbers.format(f.value(x)) + ")");
        }
        FunctionView.drawPoint(g, plane, x0.get(), f.value(x0.get()), CURVE,
                "f(" + Numbers.format(x0.get()) + ") = " + Numbers.format(f.value(x0.get())));
    }

    // Liga os pontos onde a função muda de reta: borda esquerda, cada bico e borda direita.
    // Entre dois pontos consecutivos é uma reta de verdade, então ligar com segmentos fica perfeito.
    private void drawCurve(GraphicsContext g, Plane plane, ModularSum f) {
        List<Double> xs = new ArrayList<>();
        xs.add(-RANGE);
        // Bicos que caem fora da tela ficam de fora (não adianta desenhar o que ninguém vê)
        f.breakpoints().stream().filter(x -> x > -RANGE && x < RANGE).forEach(xs::add);
        xs.add(RANGE);

        double[] px = new double[xs.size()];
        double[] py = new double[xs.size()];
        for (int i = 0; i < xs.size(); i++) {
            px[i] = plane.x(xs.get(i));
            py[i] = plane.y(f.value(xs.get(i)));
        }
        g.setStroke(CURVE);
        g.setLineWidth(3);
        g.strokePolyline(px, py, px.length);
    }

    // A linha tracejada y = c, as soluções (bolinhas) e os trechos inteiros que também resolvem (faixa grossa)
    private void drawEquationLine(GraphicsContext g, Plane plane, ModularSum f, double width) {
        double value = c.get();
        g.setStroke(EQUATION);
        g.setLineWidth(1.5);
        g.setLineDashes(6, 5);
        g.strokeLine(0, plane.y(value), width, plane.y(value));
        g.setLineDashes((double[]) null); // volta ao traço contínuo para o resto

        g.setFill(EQUATION);
        g.setFont(Font.font(12));
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.BOTTOM);
        g.fillText("y = " + Numbers.format(value), 8, plane.y(value) - 3);

        ModularSum.Solution solution = f.solve(value);
        g.setStroke(Color.web("#c2410c", 0.55));
        g.setLineWidth(8);
        // Quando f é constante e igual a c num trecho, a solução é o trecho todo: pinta uma faixa
        for (double[] segment : solution.segments()) {
            double from = Math.max(segment[0], -RANGE);
            double to = Math.min(segment[1], RANGE);
            g.strokeLine(plane.x(from), plane.y(value), plane.x(to), plane.y(value));
        }
        for (double x : solution.points()) {
            FunctionView.drawPoint(g, plane, x, value, EQUATION, "x = " + Numbers.format(x));
        }
    }
}
