package com.calculadora.modular;

import com.calculadora.modular.ui.CalculatorApp;
import javafx.application.Application;

// Por que existe esta classe se o CalculatorApp já poderia ter um main()?
// Porque o JavaFX reclama quando a classe principal herda de Application e o JAR
// roda fora do module path. Um "main" inocente aqui no meio resolve o drama.
public class Main {

    public static void main(String[] args) {
        Application.launch(CalculatorApp.class, args);
    }
}
