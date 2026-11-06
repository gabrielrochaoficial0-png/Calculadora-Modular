package com.calculadora.modular.funcao;

import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

// A tela da função modular f(x) = a·|x − h| + k: sliders de um lado, gráfico do outro
// e a resolução passo a passo embaixo, para quem gosta de ver a conta.
public class FunctionView {

    static final double RANGE = 11; // o gráfico mostra x e y de −RANGE a +RANGE (mais que isso e a tela vira um mapa)
    static final Color GRID = Color.web("#e2e4ec");
    static final Color AXIS = Color.web("#6b7086");
    static final Color CURVE = Color.web("#4f46e5");
    static final Color EQUATION = Color.web("#c2410c");
    static final Color POINT = Color.web("#059669");

    // Os valores dos sliders. Mexeu em qualquer um, a tela inteira se refaz
    private final DoubleProperty a = new SimpleDoubleProperty(1);
    private final DoubleProperty h = new SimpleDoubleProperty(0);
    private final DoubleProperty k = new SimpleDoubleProperty(0);
    private final DoubleProperty c = new SimpleDoubleProperty(4);
    private final DoubleProperty x0 = new SimpleDoubleProperty(-2);

    // null significa igualdade (f(x) = c). Sim, null com significado. Eu também não amo, mas funciona.
    private final ObjectProperty<Relation> relation = new SimpleObjectProperty<>();

    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas();
    private final Label expression = new Label();
    private final Label details = new Label();

    public FunctionView() {
        root.getStyleClass().add("root-pane");
        root.setTop(header());
        root.setLeft(controlsCard());
        root.setCenter(graphCard());

        // Qualquer mudança nos controles manda redesenhar tudo.
        // É força bruta, mas o gráfico é pequeno e ninguém nota.
        for (DoubleProperty property : List.of(a, h, k, c, x0)) {
            property.addListener(obs -> refresh());
        }
        relation.addListener(obs -> refresh());
        refresh();
    }

    public Parent root() {
        return root;
    }

    private Region header() {
        Label title = new Label("Função Modular");
        title.getStyleClass().add("title");
        Label subtitle = new Label("f(x) = a·|x − h| + k   —   mexa nos controles e veja o gráfico mudar");
        subtitle.getStyleClass().add("subtitle");
        VBox header = new VBox(2, title, subtitle);
        header.getStyleClass().add("header");
        return header;
    }

    private Region controlsCard() {
        details.setWrapText(true);
        details.getStyleClass().add("result-expression");
        expression.getStyleClass().add("function-expression");
        expression.setWrapText(true);

        VBox display = new VBox(6, expression);
        display.getStyleClass().add("result-display");

        VBox card = new VBox(14,
                display,
                slider("a  (abertura e sentido)", a, -5, 5),
                slider("h  (desloca na horizontal)", h, -8, 8),
                slider("k  (desloca na vertical)", k, -8, 8),
                relationSelector(),
                slider("c  (lado direito)", c, -10, 10),
                slider("x  (calcular f(x))", x0, -10, 10));
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        BorderPane.setMargin(card, new Insets(0, 16, 0, 0));
        return card;
    }

    // A caixinha para escolher entre =, <, ≤, > e ≥
    private Region relationSelector() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll("=", "<", "≤", ">", "≥");
        box.setValue("=");
        box.setMaxWidth(Double.MAX_VALUE);
        box.valueProperty().addListener((obs, old, symbol) -> {
            // Começa em "igualdade" (null) e só troca se achar o símbolo na lista de relações
            relation.set(null);
            for (Relation option : Relation.values()) {
                if (option.symbol().equals(symbol)) {
                    relation.set(option);
                }
            }
        });
        Label label = new Label("Resolver  f(x) ? c");
        label.getStyleClass().add("section-label");
        return new VBox(4, label, box);
    }

    // Slider com nome e valor atual ao lado. Também é usado pela SumView, por isso não é private
    static VBox slider(String name, DoubleProperty property, double min, double max) {
        Slider slider = new Slider(min, max, property.get());
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(1); // um tick extra entre cada inteiro dá passo de 0,5
        slider.setSnapToTicks(true); // sem isso o valor viria 2,3718... e o texto ficaria horrível
        slider.valueProperty().bindBidirectional(property);

        Label value = new Label();
        value.getStyleClass().add("slider-value");
        value.textProperty().bind(property.map(number -> Numbers.format(number.doubleValue())));

        Label label = new Label(name);
        label.getStyleClass().add("section-label");
        Region spacer = new Region();
        HBox heading = new HBox(label, spacer, value);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return new VBox(4, heading, slider);
    }

    private Region graphCard() {
        // O canvas se estica junto com o cartão, e redesenha toda vez que o tamanho muda
        Pane graph = new Pane(canvas);
        canvas.widthProperty().bind(graph.widthProperty());
        canvas.heightProperty().bind(graph.heightProperty());
        canvas.widthProperty().addListener(obs -> draw());
        canvas.heightProperty().addListener(obs -> draw());

        details.setMinHeight(Region.USE_PREF_SIZE);
        VBox card = new VBox(12, graph, details);
        VBox.setVgrow(graph, Priority.ALWAYS);
        card.getStyleClass().add("card");
        return card;
    }

    // Atualiza os textos e o desenho. Chamado a cada mexida de slider
    private void refresh() {
        if (a.get() == 0) {
            // a = 0 mata o módulo, então em vez de quebrar a gente explica o que houve
            expression.setText("a não pode ser 0");
            details.setText("Com a = 0 a função vira uma constante e deixa de ser modular. Escolha outro valor de a.");
            draw();
            return;
        }
        ModularFunction f = function();
        expression.setText(f.expression());
        details.setText(describe(f));
        draw();
    }

    private ModularFunction function() {
        return new ModularFunction(a.get(), h.get(), k.get());
    }

    // Monta o textão de baixo: vértice, domínio, imagem, partes, valor em x e a resolução
    private String describe(ModularFunction f) {
        StringBuilder text = new StringBuilder();
        text.append("Vértice: V(").append(Numbers.format(f.h())).append(", ").append(Numbers.format(f.k())).append(")")
                .append(f.opensUp() ? "  (ponto mais baixo)" : "  (ponto mais alto)").append('\n');
        text.append("Domínio: ℝ      Imagem: ").append(f.image()).append('\n');

        List<String> parts = f.piecewise();
        text.append("Sem o módulo:  ").append(parts.get(0)).append("   |   ").append(parts.get(1)).append("\n\n");

        double x = x0.get();
        text.append("Valor em x = ").append(Numbers.format(x)).append(":  f(").append(Numbers.format(x)).append(") = ")
                .append(Numbers.format(f.value(x))).append("\n\n");

        // Igualdade ou inequação? Cada uma tem o seu passo a passo
        Relation sign = relation.get();
        text.append("Resolvendo f(x) ").append(sign == null ? "=" : sign.symbol()).append(' ')
                .append(Numbers.format(c.get())).append('\n');
        List<String> steps = sign == null ? f.solveSteps(c.get()) : f.solveInequality(sign, c.get()).steps();
        for (String step : steps) {
            text.append("   ").append(step).append('\n');
        }
        if (sign != null) {
            text.append("   S = ").append(f.solveInequality(sign, c.get()).set()).append('\n');
        }
        return text.toString().stripTrailing();
    }

    // ---- desenho do gráfico ----

    private void draw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        // Antes da janela aparecer o canvas tem tamanho zero, e desenhar no nada dá erro
        if (width <= 0 || height <= 0) {
            return;
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, width, height);

        // A escala usa o menor lado para o gráfico ficar quadrado (senão o V ficaria esticado)
        double scale = Math.min(width, height) / (2 * RANGE);
        Plane plane = new Plane(width / 2, height / 2, scale);

        drawGrid(g, plane, width, height);
        if (a.get() == 0) {
            return; // só a grade, sem função para desenhar
        }
        ModularFunction f = function();
        // A ordem importa: o que é desenhado depois fica por cima
        drawEquationLine(g, plane, f, width);
        drawCurve(g, plane, f);
        drawPoint(g, plane, f.h(), f.k(), POINT, "V(" + Numbers.format(f.h()) + ", " + Numbers.format(f.k()) + ")");
        drawPoint(g, plane, x0.get(), f.value(x0.get()), CURVE,
                "f(" + Numbers.format(x0.get()) + ") = " + Numbers.format(f.value(x0.get())));
    }

    // Grade e eixos com os números de 2 em 2. Também é usado pela SumView
    static void drawGrid(GraphicsContext g, Plane plane, double width, double height) {
        g.setLineWidth(1);
        for (int i = (int) -RANGE; i <= RANGE; i++) {
            g.setStroke(i == 0 ? AXIS : GRID); // o eixo (i = 0) é mais escuro que as linhas da grade
            g.strokeLine(plane.x(i), 0, plane.x(i), height);
            g.strokeLine(0, plane.y(i), width, plane.y(i));
        }

        g.setFill(AXIS);
        g.setFont(Font.font(11));
        // Números do eixo x, logo abaixo da linha
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.TOP);
        for (int i = -10; i <= 10; i += 2) {
            if (i != 0) { // o zero fica de fora para não amontoar no encontro dos eixos
                g.fillText(String.valueOf(i), plane.x(i), plane.y(0) + 4);
            }
        }
        // Números do eixo y, encostados na esquerda da linha
        g.setTextAlign(TextAlignment.RIGHT);
        g.setTextBaseline(VPos.CENTER);
        for (int i = -10; i <= 10; i += 2) {
            if (i != 0) {
                g.fillText(String.valueOf(i), plane.x(0) - 5, plane.y(i));
            }
        }
    }

    // Como f é feita de duas retas, basta ligar três pontos: borda esquerda, vértice e borda direita.
    // Nada de calcular mil pontinhos para fingir uma curva.
    private void drawCurve(GraphicsContext g, Plane plane, ModularFunction f) {
        g.setStroke(CURVE);
        g.setLineWidth(3);
        g.strokePolyline(
                new double[] {plane.x(-RANGE), plane.x(f.h()), plane.x(RANGE)},
                new double[] {plane.y(f.value(-RANGE)), plane.y(f.k()), plane.y(f.value(RANGE))},
                3);
    }

    // A linha tracejada y = c e tudo que depende dela: as soluções (bolinhas) e o sombreado da inequação
    private void drawEquationLine(GraphicsContext g, Plane plane, ModularFunction f, double width) {
        double value = c.get();
        g.setStroke(EQUATION);
        g.setLineWidth(1.5);
        g.setLineDashes(6, 5);
        g.strokeLine(0, plane.y(value), width, plane.y(value));
        g.setLineDashes((double[]) null); // desliga o tracejado, senão o resto do gráfico sai pontilhado

        g.setFill(EQUATION);
        g.setFont(Font.font(12));
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.BOTTOM);
        g.fillText("y = " + Numbers.format(value), 8, plane.y(value) - 3);

        Relation sign = relation.get();
        if (sign != null) {
            shade(g, plane, f.solveInequality(sign, value).intervals());
        }
        // Onde a linha cruza o V, nasce uma solução (pode ser 0, 1 ou 2 bolinhas)
        for (double solution : f.solve(value)) {
            drawPoint(g, plane, solution, value, EQUATION, "x = " + Numbers.format(solution));
        }
    }

    // Pinta no eixo x os valores de x que satisfazem a inequação (a faixa grossa e translúcida)
    private void shade(GraphicsContext g, Plane plane, List<double[]> intervals) {
        g.setStroke(Color.web("#c2410c", 0.55));
        g.setLineWidth(8);
        for (double[] interval : intervals) {
            // Intervalos com ∞ precisam ser cortados na borda, já que a tela é finita (por enquanto)
            double from = Math.max(interval[0], -RANGE);
            double to = Math.min(interval[1], RANGE);
            g.strokeLine(plane.x(from), plane.y(0), plane.x(to), plane.y(0));
        }
    }

    // Uma bolinha colorida com um textinho do lado. Também é usado pela SumView
    static void drawPoint(GraphicsContext g, Plane plane, double x, double y, Color color, String label) {
        double px = plane.x(x);
        double py = plane.y(y);
        g.setFill(color);
        g.fillOval(px - 5, py - 5, 10, 10);
        g.setFont(Font.font(12));
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.BOTTOM);
        g.fillText(label, px + 8, py - 6);
    }

    // O tradutor entre dois mundos: coordenadas do plano (onde a matemática mora) e pixels do canvas.
    // Detalhe importante: o y dos pixels cresce para BAIXO, então a gente subtrai.
    record Plane(double originX, double originY, double scale) {
        double x(double value) {
            return originX + value * scale;
        }

        double y(double value) {
            return originY - value * scale;
        }
    }
}
