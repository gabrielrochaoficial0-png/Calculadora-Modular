package com.calculadora.modular.ui;

import com.calculadora.modular.core.ModularCalculator;
import com.calculadora.modular.funcao.FunctionView;
import com.calculadora.modular.funcao.SumView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

// A janela principal: só junta as três abas e abre o palco. Cortinas, por favor.
public class CalculatorApp extends Application {

    @Override
    public void start(Stage stage) {
        CalculatorViewModel viewModel = new CalculatorViewModel(new ModularCalculator());

        // Três abas, duas "modulares" que não têm nada a ver uma com a outra (|x| vs. mod).
        // Sim, o nome confunde. Não, não vamos renomear a esta altura.
        TabPane tabs = new TabPane(
                tab("Função modular |x|", new FunctionView().root()),
                tab("Soma de módulos", new SumView().root()),
                tab("Aritmética modular (mod)", new CalculatorView(viewModel).root()));

        Scene scene = new Scene(tabs, 1000, 640);
        scene.getStylesheets().add(getClass().getResource("calculator.css").toExternalForm());

        stage.setTitle("Calculadora Modular");
        // Abaixo disso os controles começam a se espremer e a coisa fica feia
        stage.setMinWidth(860);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    // Cria uma aba sem o botãozinho de fechar (fechar tudo e ficar sem calculadora seria um anticlímax)
    private static Tab tab(String title, Parent content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }
}
