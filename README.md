# Calculadora Modular

Um app JavaFX com **duas** ideias de "modular" que não têm nada a ver uma com a outra,
só o nome. Escolha a sua aba:

| Aba | "Modular" de quê? | Para que serve |
|-----|-------------------|----------------|
| Função modular \|x\| | Módulo (valor absoluto) | Ver o "V" mexer e resolver equações e inequações passo a passo |
| Soma de módulos | Vários módulos juntos | Funções com mais de um "bico", resolvidas trecho a trecho |
| Aritmética modular (mod) | Resto da divisão | Contas em ℤₘ com inteiros de qualquer tamanho |

> Sim, o nome é confuso. Não, não vamos renomear a esta altura.

## Aba "Função modular |x|"

Estuda funções da forma **f(x) = a·|x − h| + k**. Os sliders de `a`, `h` e `k`
movem o gráfico em tempo real:

- `a` muda a abertura do V (negativo vira o V de cabeça para baixo);
- `h` empurra o vértice para os lados;
- `k` empurra o vértice para cima e para baixo.

Embaixo do gráfico a tela mostra:

- a lei da função e o vértice V(h, k);
- domínio, imagem e a definição por partes (sem o módulo);
- o valor de f em um ponto `x` escolhido;
- a resolução passo a passo de **f(x) = c**, com as soluções marcadas no gráfico
  (a linha laranja tracejada é o `y = c`, e onde ela cruza o V nasce uma solução);
- o mesmo para inequações **f(x) < c, ≤, >, ≥**: passo a passo, conjunto solução em
  notação de intervalo (ex.: `(−∞, −2] ∪ [8, +∞)`) e o trecho do eixo x sombreado.

Com `a = 0` o módulo desaparece e sobra uma reta, então a tela avisa e se recusa a
desenhar. É a única coisa que ela recusa.

O núcleo matemático está em `funcao/ModularFunction.java` e é coberto por
`ModularFunctionTest`.

## Aba "Soma de módulos"

Estuda **f(x) = a·|x − p| + b·|x − q| + k**. Mostra os "bicos" do gráfico, a função
reescrita sem módulos (uma reta para cada trecho), a imagem e a resolução de
**f(x) = c** trecho a trecho, descartando soluções que caem fora do trecho (o ✗ é uma
solução de mentira). Quando f é constante igual a c num trecho, a solução é o
intervalo inteiro (sombreado).

Aqui o gráfico **não precisa ter cara de V**. O formato depende das inclinações:

| Situação | Formato |
|----------|---------|
| `a` e `b` positivos | "U" com fundo reto entre `p` e `q` (se `a = b`) |
| `a + b = 0` | pontas planas, tipo uma rampa ou degrau |
| `p = q` | os dois módulos viram um só V |
| `p = q` e `a + b = 0` | uma reta horizontal, sem graça |

Núcleo em `funcao/ModularSum.java`, coberto por `ModularSumTest`.

## Aba "Aritmética modular (mod)"

Trabalha com inteiros de qualquer tamanho (`BigInteger`) e sempre devolve o resultado
em `[0, m)`, mesmo quando a conta dá negativa (o `%` do Java não faz isso, essa é a
pegadinha clássica).

| Botão | Operação | Observação |
|-------|----------|------------|
| `a + b` | Soma | |
| `a − b` | Subtração | |
| `a × b` | Multiplicação | |
| `aᵇ` | Potência | Expoente negativo usa o inverso de `a` |
| `a⁻¹` | Inverso | Só existe quando `mdc(a, m) = 1`; senão a calculadora avisa em vez de inventar |

Cada cálculo vai para o histórico. Clicar em uma linha do histórico recarrega os
valores nos campos. `Enter` em qualquer campo calcula, e espaços dentro do número são
ignorados (`1 000` vira `1000`).

## Como rodar no Windows

Requisito: **JDK 21 ou mais recente** (por exemplo, o
[Temurin 21](https://adoptium.net/temurin/releases/?version=21)). Na instalação,
marque a opção que define `JAVA_HOME`. Não é preciso instalar o Maven nem o
JavaFX: o `mvnw.cmd` baixa o Maven e o Maven baixa o JavaFX.
(Na primeira vez demora um pouquinho, porque ele baixa metade da internet. Nas
próximas, abre rápido.)

No Prompt de Comando ou no PowerShell, dentro da pasta do projeto:

```bat
mvnw.cmd javafx:run
```

Para rodar os testes:

```bat
mvnw.cmd test
```

No Linux ou macOS, use `./mvnw` no lugar de `mvnw.cmd`.

> Dica: no PowerShell o comando precisa de `.\` na frente (`.\mvnw.cmd javafx:run`).
> No Git Bash, o `mvnw.cmd` não é reconhecido, então use o PowerShell ou o
> Prompt de Comando.

### Pela IDE

Abra a pasta como projeto Maven (IntelliJ IDEA, Eclipse ou VS Code) e rode a
classe `com.calculadora.modular.Main`. Ela existe só para o JavaFX não reclamar do
module path, então não estranhe o `main` quase vazio.

## Estrutura

```
src/main/java/com/calculadora/modular/
├── Main.java                      inicializador
├── core/                          contas do mod, sem JavaFX
│   ├── Operation.java             operações e se precisam de b
│   ├── ModularCalculator.java     os cálculos
│   └── Calculation.java           resultado imutável + expressão "a + b ≡ r (mod m)"
├── funcao/                        funções com módulo |x| (o outro "modular")
│   ├── ModularFunction.java       f(x) = a·|x − h| + k: valor, imagem, equações e inequações
│   ├── ModularSum.java            f(x) = a·|x − p| + b·|x − q| + k, resolvida por trechos
│   ├── Relation.java              <, ≤, > e ≥ (e como o sinal inverte ao dividir por negativo)
│   ├── Numbers.java               formata números como gente escreve ("−1,5", e não "-1.5")
│   ├── FunctionView.java          tela do V: sliders, gráfico e passo a passo
│   └── SumView.java               tela da soma de módulos
└── ui/                            janela e aba do mod (padrão MVVM)
    ├── CalculatorApp.java         monta a janela e as três abas
    ├── CalculatorView.java        componentes visuais e bindings
    └── CalculatorViewModel.java   estado da tela, testável sem abrir janela
src/main/resources/.../ui/calculator.css   tema visual
```

Duas decisões que valem uma explicação:

- Os pacotes `core` e `funcao` (a parte de matemática) não conhecem a interface, então
  dá para trocar JavaFX por outra tela sem mexer nos cálculos. Só as classes `*View`
  falam com o JavaFX.
- Na aba do mod, a view só se liga a propriedades do view model, e toda a lógica da
  tela é coberta por testes em `CalculatorViewModelTest`, sem precisar abrir janela
  nem clicar em botão na mão.

## Testes

Cada parte de matemática tem o seu teste, com a conta feita à mão nos comentários:
`ModularCalculatorTest`, `CalculatorViewModelTest`, `ModularFunctionTest` e
`ModularSumTest`. Se mexer em alguma fórmula e alguma coisa quebrar, é para isso que
eles existem. Não leve para o lado pessoal.
