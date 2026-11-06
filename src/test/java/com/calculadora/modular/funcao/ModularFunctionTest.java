package com.calculadora.modular.funcao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

// Testes da função |x|. Os comentários mostram a conta feita à mão, para você não precisar confiar em mim
class ModularFunctionTest {

    @Test
    void calculaValorDaFuncao() {
        ModularFunction f = new ModularFunction(2, 3, 1); // 2|x − 3| + 1
        assertEquals(1, f.value(3));
        assertEquals(11, f.value(8));
        assertEquals(7, f.value(0));
    }

    @Test
    void naoAceitaCoeficienteZero() {
        assertThrows(IllegalArgumentException.class, () -> new ModularFunction(0, 1, 1));
    }

    @Test
    void resolveEquacaoComDuasSolucoes() {
        // |x − 3| = 5  ->  x = −2 ou x = 8
        assertEquals(List.of(-2.0, 8.0), new ModularFunction(1, 3, 0).solve(5));
    }

    @Test
    void resolveEquacaoComUmaSolucao() {
        assertEquals(List.of(3.0), new ModularFunction(2, 3, 1).solve(1));
    }

    @Test
    void equacaoSemSolucaoQuandoModuloDariaNegativo() {
        assertTrue(new ModularFunction(1, 0, 0).solve(-4).isEmpty());
        assertTrue(new ModularFunction(-1, 0, 2).solve(5).isEmpty()); // máximo é 2
    }

    @Test
    void imagemDependeDoSentidoDaAbertura() {
        assertEquals("[1, +∞)", new ModularFunction(2, 3, 1).image());
        assertEquals("(−∞, 1]", new ModularFunction(-2, 3, 1).image());
    }

    @Test
    void formataExpressao() {
        assertEquals("f(x) = |x|", new ModularFunction(1, 0, 0).expression());
        assertEquals("f(x) = 2|x − 3| + 1", new ModularFunction(2, 3, 1).expression());
        assertEquals("f(x) = −|x + 2| − 4", new ModularFunction(-1, -2, -4).expression());
    }

    @Test
    void defineFuncaoPorPartes() {
        // 2|x − 3| + 1:  x ≥ 3 -> 2x − 5;  x < 3 -> −2x + 7
        assertEquals(List.of("2x − 5,  se x ≥ 3", "−2x + 7,  se x < 3"),
                new ModularFunction(2, 3, 1).piecewise());
    }

    @Test
    void inequacaoMenorDaIntervalo() {
        // |x − 3| < 5  ->  (−2, 8)
        assertEquals("(−2, 8)", new ModularFunction(1, 3, 0).solveInequality(Relation.LESS, 5).set());
        assertEquals("[−2, 8]", new ModularFunction(1, 3, 0).solveInequality(Relation.LESS_OR_EQUAL, 5).set());
    }

    @Test
    void inequacaoMaiorDaDoisIntervalos() {
        // |x − 3| ≥ 5  ->  (−∞, −2] ∪ [8, +∞)
        assertEquals("(−∞, −2] ∪ [8, +∞)",
                new ModularFunction(1, 3, 0).solveInequality(Relation.GREATER_OR_EQUAL, 5).set());
    }

    @Test
    void inequacaoComModuloImpossivelOuSempreVerdadeiro() {
        assertEquals("∅ (nenhum x serve)", new ModularFunction(1, 0, 0).solveInequality(Relation.LESS, -1).set());
        assertEquals("ℝ (todo x serve)", new ModularFunction(1, 0, 0).solveInequality(Relation.GREATER, -1).set());
        assertEquals("ℝ − {0}", new ModularFunction(1, 0, 0).solveInequality(Relation.GREATER, 0).set());
    }

    @Test
    void coeficienteNegativoInverteOSinal() {
        // −|x| + 4 > 1  ->  |x| < 3  ->  (−3, 3)
        assertEquals("(−3, 3)", new ModularFunction(-1, 0, 4).solveInequality(Relation.GREATER, 1).set());
    }
}
