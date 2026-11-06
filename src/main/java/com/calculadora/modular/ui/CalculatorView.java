package com.calculadora.modular.ui;

import com.calculadora.modular.core.Calculation;
import com.calculadora.modular.core.Operation;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// A parte visual da aba "mod": monta os componentes e liga cada um ao view model.
// Aqui não tem conta nenhuma, só tinta e fio.
public class CalculatorView {

    // Marca usada pelo CSS para pintar a mensagem de erro de laranja-bravo
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private final CalculatorViewModel viewModel;
    private final BorderPane root = new BorderPane();

    public CalculatorView(CalculatorViewModel viewModel) {
        this.viewModel = viewModel;
        root.getStyleClass().add("root-pane");
        root.setTop(header());
        root.setCenter(calculatorCard());

        Region history = historyPanel();
        BorderPane.setMargin(history, new Insets(0, 0, 0, 16));
        root.setRight(history);
    }

    public Parent root() {
        return root;
    }

    private Region header() {
        Label title = new Label("Calculadora Modular");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Aritmética em ℤₘ com inteiros de qualquer tamanho");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(2, title, subtitle);
        header.getStyleClass().add("header");
        return header;
    }

    private Region calculatorCard() {
        VBox card = new VBox(18,
                sectionLabel("Operação"), operationSelector(),
                operandFields(),
                calculateButton(),
                resultDisplay());
        card.getStyleClass().add("card");
        return card;
    }

    private Region operationSelector() {
        ToggleGroup group = new ToggleGroup();
        HBox selector = new HBox();
        selector.getStyleClass().add("segmented");

        // Um botão para cada operação. Guardamos a operação dentro do botão para achá-la depois
        for (Operation operation : Operation.values()) {
            ToggleButton button = new ToggleButton(operation.buttonLabel());
            button.setUserData(operation);
            button.setToggleGroup(group);
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);
            selector.getChildren().add(button);
        }

        // Botão clicado -> view model.
        // Clicar no botão que já está marcado desmarcaria tudo, então a gente desfaz isso na hora
        group.selectedToggleProperty().addListener((obs, previous, selected) -> {
            if (selected == null) {
                group.selectToggle(previous);
            } else {
                viewModel.operationProperty().set((Operation) selected.getUserData());
            }
        });

        // View model -> botão (acontece quando um item do histórico é restaurado)
        viewModel.operationProperty().addListener((obs, old, operation) -> selectToggleFor(group, operation));
        selectToggleFor(group, viewModel.operationProperty().get());
        return selector;
    }

    // Procura o botão da operação e marca ele. Sem mistério, só uma busca na lista
    private static void selectToggleFor(ToggleGroup group, Operation operation) {
        for (var toggle : group.getToggles()) {
            if (toggle.getUserData() == operation) {
                group.selectToggle(toggle);
            }
        }
    }

    private Region operandFields() {
        VBox secondOperand = labeledField("b", "Ex.: 5", viewModel.secondOperandProperty());
        // Quando a operação não usa b, o campo some E devolve o espaço (senão sobraria um buraco esquisito)
        secondOperand.visibleProperty().bind(viewModel.secondOperandRequiredBinding());
        secondOperand.managedProperty().bind(secondOperand.visibleProperty());

        HBox fields = new HBox(12,
                labeledField("a", "Ex.: 7", viewModel.firstOperandProperty()),
                secondOperand,
                labeledField("m (módulo)", "Ex.: 13", viewModel.modulusProperty()));
        fields.getChildren().forEach(field -> HBox.setHgrow(field, Priority.ALWAYS));
        return fields;
    }

    // Um campo de texto com o rótulo em cima, já ligado ao valor do view model
    private VBox labeledField(String label, String prompt, StringProperty value) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.textProperty().bindBidirectional(value);
        field.setOnAction(event -> viewModel.calculate()); // Enter também calcula, ninguém precisa pegar o mouse
        return new VBox(6, sectionLabel(label), field);
    }

    private Button calculateButton() {
        Button button = new Button("Calcular");
        button.getStyleClass().add("primary");
        button.setDefaultButton(true);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> viewModel.calculate());
        return button;
    }

    private Region resultDisplay() {
        Label result = new Label();
        result.getStyleClass().add("result-value");
        // Sem resultado ainda? Mostra um travessão em vez de um buraco vazio
        result.textProperty().bind(Bindings.when(viewModel.resultProperty().isEmpty())
                .then("—")
                .otherwise(viewModel.resultProperty()));

        // Se der erro, a mensagem toma o lugar da expressão (os dois nunca aparecem juntos)
        Label expression = new Label();
        expression.getStyleClass().add("result-expression");
        expression.setWrapText(true);
        expression.textProperty().bind(Bindings.when(viewModel.errorProperty().isEmpty())
                .then(viewModel.expressionProperty())
                .otherwise(viewModel.errorProperty()));
        viewModel.errorProperty().addListener((obs, old, message) ->
                expression.pseudoClassStateChanged(ERROR, !message.isEmpty()));

        VBox display = new VBox(4, sectionLabel("Resultado"), result, expression);
        display.getStyleClass().add("result-display");
        return display;
    }

    private Region historyPanel() {
        ListView<Calculation> list = new ListView<>(viewModel.history());
        list.setPlaceholder(new Label("Nenhum cálculo ainda"));
        // Cada linha do histórico mostra a expressão pronta do cálculo
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Calculation calculation, boolean empty) {
                super.updateItem(calculation, empty);
                setText(empty ? null : calculation.expression());
            }
        });
        // Clicar num item recarrega o cálculo nos campos (só com o botão esquerdo, o direito fica de folga)
        list.setOnMouseClicked(event -> {
            Calculation selected = list.getSelectionModel().getSelectedItem();
            if (event.getButton() == MouseButton.PRIMARY && selected != null) {
                viewModel.restore(selected);
            }
        });
        VBox.setVgrow(list, Priority.ALWAYS);

        // Botão "Limpar": só funciona se houver o que limpar (apagar o nada seria filosófico demais)
        Button clear = new Button("Limpar");
        clear.getStyleClass().add("ghost");
        clear.disableProperty().bind(Bindings.isEmpty(viewModel.history()));
        clear.setOnAction(event -> viewModel.clearHistory());

        Region spacer = new Region(); // um espaçador elástico que empurra o botão para a direita
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox heading = new HBox(sectionLabel("Histórico"), spacer, clear);
        heading.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(10, heading, list);
        panel.getStyleClass().add("history");
        return panel;
    }

    // Rótulinho cinza e pequeno que aparece em tudo quanto é canto
    private static Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }
}
