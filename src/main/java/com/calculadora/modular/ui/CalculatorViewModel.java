package com.calculadora.modular.ui;

import com.calculadora.modular.core.Calculation;
import com.calculadora.modular.core.ModularCalculator;
import com.calculadora.modular.core.Operation;
import java.math.BigInteger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// O "estado" da tela e o botão de calcular, sem um único componente visual.
// Isso permite testar tudo sem abrir janela (e sem ficar clicando em botão na mão).
public class CalculatorViewModel {

    private final ModularCalculator calculator;

    // O que o usuário escolheu e digitou. Os números ficam como texto até a hora de calcular
    private final ObjectProperty<Operation> operation = new SimpleObjectProperty<>(Operation.ADD);
    private final StringProperty firstOperand = new SimpleStringProperty("");
    private final StringProperty secondOperand = new SimpleStringProperty("");
    private final StringProperty modulus = new SimpleStringProperty("");

    // O que a tela mostra de volta: ou resultado + expressão, ou um erro
    private final StringProperty result = new SimpleStringProperty("");
    private final StringProperty expression = new SimpleStringProperty("");
    private final StringProperty error = new SimpleStringProperty("");

    // A view usa isto para esconder o campo "b" no inverso (que não precisa dele)
    private final BooleanBinding secondOperandRequired =
            Bindings.createBooleanBinding(() -> operation.get().requiresSecondOperand(), operation);

    private final ObservableList<Calculation> history = FXCollections.observableArrayList();

    public CalculatorViewModel(ModularCalculator calculator) {
        this.calculator = calculator;
    }

    public void calculate() {
        try {
            BigInteger a = parse(firstOperand.get(), "a");
            // Se a operação não usa b, nem olha o que tem escrito lá
            BigInteger b = secondOperandRequired.get() ? parse(secondOperand.get(), "b") : null;
            BigInteger m = parse(modulus.get(), "m");

            Calculation calculation = calculator.calculate(operation.get(), a, b, m);
            show(calculation);
            history.add(0, calculation); // o mais recente fica no topo
        } catch (IllegalArgumentException e) {
            // Deu ruim: limpa o resultado antigo para não ficar mentindo na tela
            result.set("");
            expression.set("");
            error.set(e.getMessage());
        }
    }

    // Clicou num item do histórico? Devolve tudo para os campos, como se nada tivesse acontecido
    public void restore(Calculation calculation) {
        operation.set(calculation.operation());
        firstOperand.set(calculation.firstOperand().toString());
        secondOperand.set(calculation.secondOperand() == null ? "" : calculation.secondOperand().toString());
        modulus.set(calculation.modulus().toString());
        show(calculation);
    }

    public void clearHistory() {
        history.clear();
    }

    private void show(Calculation calculation) {
        result.set(calculation.result().toString());
        expression.set(calculation.expression());
        error.set("");
    }

    // Transforma o texto digitado em número, com mensagens de erro que um humano entende
    private static BigInteger parse(String text, String field) {
        String digits = text.replaceAll("\\s+", ""); // "1 000" vira "1000", porque ninguém merece contar zeros
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("Informe o valor de " + field + ".");
        }
        try {
            return new BigInteger(digits);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + text.strip() + "\" não é um inteiro válido para " + field + ".");
        }
    }

    // ---- daqui para baixo é só encanamento: getters que a view usa para se ligar ----

    public ObjectProperty<Operation> operationProperty() {
        return operation;
    }

    public StringProperty firstOperandProperty() {
        return firstOperand;
    }

    public StringProperty secondOperandProperty() {
        return secondOperand;
    }

    public StringProperty modulusProperty() {
        return modulus;
    }

    public StringProperty resultProperty() {
        return result;
    }

    public StringProperty expressionProperty() {
        return expression;
    }

    public StringProperty errorProperty() {
        return error;
    }

    public BooleanBinding secondOperandRequiredBinding() {
        return secondOperandRequired;
    }

    public ObservableList<Calculation> history() {
        return history;
    }
}
