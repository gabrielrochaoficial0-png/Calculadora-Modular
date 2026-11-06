package com.calculadora.modular.funcao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

// Testes da soma de módulos. A função padrão tem um fundo reto entre os bicos, ótimo para testar trechos constantes
class ModularSumTest {

    // |x + 2| + |x − 3|
    private final ModularSum f = new ModularSum(1, -2, 1, 3, 0);

    @Test
    void calculaValor() {
        assertEquals(5, f.value(-2));
        assertEquals(5, f.value(3));
        assertEquals(9, f.value(5));
    }

    @Test
    void encontraTrechosRetos() {
        assertEquals(List.of("−2x + 1,  se x < −2", "5,  se −2 ≤ x < 3", "2x − 1,  se x ≥ 3"), f.piecewise());
    }

    @Test
    void resolveEquacaoEmDoisPontos() {
        // |x + 2| + |x − 3| = 7  ->  x = −3 ou x = 4
        assertEquals(List.of(-3.0, 4.0), f.solve(7).points());
    }

    // Entre −2 e 3 a função vale 5 o tempo todo, então f(x) = 5 é resolvida por todos os x do trecho
    @Test
    void trechoConstanteDaSegmentoComoSolucao() {
        ModularSum.Solution solution = f.solve(5);
        assertTrue(solution.points().isEmpty());
        assertEquals(1, solution.segments().size());
        assertEquals(-2, solution.segments().get(0)[0]);
        assertEquals(3, solution.segments().get(0)[1]);
    }

    @Test
    void semSolucaoAbaixoDoMinimo() {
        assertTrue(f.solve(4).points().isEmpty());
        assertEquals("[5, +∞)", f.image());
    }

    @Test
    void imagemQuandoOsCoeficientesSeCancelam() {
        // |x − 1| − |x + 2|: vai de −3 a 3
        assertEquals("[−3, 3]", new ModularSum(1, 1, -1, -2, 0).image());
    }

    // Dois bicos no mesmo lugar não são dois bicos: são um só (e dois trechos, não três)
    @Test
    void bicosIguaisViramUmSo() {
        assertEquals(List.of(1.0), new ModularSum(1, 1, 2, 1, 0).breakpoints());
        assertEquals(2, new ModularSum(1, 1, 2, 1, 0).pieces().size());
    }

    @Test
    void formataExpressao() {
        assertEquals("f(x) = 2|x − 1| − |x + 2| + 3", new ModularSum(2, 1, -1, -2, 3).expression());
    }
}
