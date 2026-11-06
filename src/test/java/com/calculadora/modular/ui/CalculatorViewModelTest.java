package com.calculadora.modular.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.calculadora.modular.core.ModularCalculator;
import com.calculadora.modular.core.Operation;

// Testa a lógica da tela sem abrir tela nenhuma. Foi para isso que o view model foi inventado
class CalculatorViewModelTest {

    private final CalculatorViewModel viewModel = new CalculatorViewModel(new ModularCalculator());

    @Test
    void showsResultAndRecordsHistory() {
        fill(Operation.MULTIPLY, "6", "7", "10");

        viewModel.calculate();

        assertEquals("2", viewModel.resultProperty().get());
        assertEquals("6 × 7 ≡ 2 (mod 10)", viewModel.expressionProperty().get());
        assertEquals("", viewModel.errorProperty().get());
        assertEquals(1, viewModel.history().size());
    }

    // No inverso, o que estiver escrito em "b" é ignorado (mesmo que seja lixo)
    @Test
    void ignoresSecondOperandForUnaryOperation() {
        fill(Operation.INVERSE, "3", "texto qualquer", "11");

        viewModel.calculate();

        assertEquals("4", viewModel.resultProperty().get());
    }

    @Test
    void acceptsSpacesInsideNumbers() {
        fill(Operation.ADD, " 1 000 ", "1", "7");

        viewModel.calculate();

        assertEquals("0", viewModel.resultProperty().get());
    }

    @Test
    void reportsMissingInput() {
        fill(Operation.ADD, "1", "", "7");

        viewModel.calculate();

        assertEquals("Informe o valor de b.", viewModel.errorProperty().get());
        assertEquals("", viewModel.resultProperty().get());
        assertTrue(viewModel.history().isEmpty());
    }

    // Vírgula não vale: inteiro é inteiro
    @Test
    void reportsInvalidNumber() {
        fill(Operation.ADD, "1,5", "1", "7");

        viewModel.calculate();

        assertEquals("\"1,5\" não é um inteiro válido para a.", viewModel.errorProperty().get());
    }

    @Test
    void reportsArithmeticErrors() {
        fill(Operation.INVERSE, "4", "", "10");

        viewModel.calculate();

        assertEquals("4 não tem inverso módulo 10, pois mdc(4, 10) ≠ 1.", viewModel.errorProperty().get());
    }

    @Test
    void restoresCalculationFromHistory() {
        fill(Operation.POWER, "3", "200", "13");
        viewModel.calculate();
        fill(Operation.ADD, "", "", "");

        viewModel.restore(viewModel.history().get(0));

        assertEquals(Operation.POWER, viewModel.operationProperty().get());
        assertEquals("3", viewModel.firstOperandProperty().get());
        assertEquals("200", viewModel.secondOperandProperty().get());
        assertEquals("13", viewModel.modulusProperty().get());
        assertEquals("9", viewModel.resultProperty().get());
    }

    @Test
    void newestCalculationComesFirstAndHistoryCanBeCleared() {
        fill(Operation.ADD, "1", "1", "7");
        viewModel.calculate();
        fill(Operation.ADD, "2", "2", "7");
        viewModel.calculate();

        assertEquals("2 + 2 ≡ 4 (mod 7)", viewModel.history().get(0).expression());

        viewModel.clearHistory();

        assertTrue(viewModel.history().isEmpty());
    }

    @Test
    void secondOperandIsRequiredOnlyForBinaryOperations() {
        viewModel.operationProperty().set(Operation.INVERSE);
        assertFalse(viewModel.secondOperandRequiredBinding().get());

        viewModel.operationProperty().set(Operation.POWER);
        assertTrue(viewModel.secondOperandRequiredBinding().get());
    }

    // Preenche os campos de uma vez, como um usuário apressado
    private void fill(Operation operation, String a, String b, String m) {
        viewModel.operationProperty().set(operation);
        viewModel.firstOperandProperty().set(a);
        viewModel.secondOperandProperty().set(b);
        viewModel.modulusProperty().set(m);
    }
}
