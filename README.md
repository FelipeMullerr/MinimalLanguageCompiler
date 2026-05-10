# MinimalIDE

IDE para compilação e análise semântica de uma linguagem de programação customizada, desenvolvida como trabalho da disciplina de Compiladores — UNIVALI.

## Requisitos

- Java 21 ou superior
- Maven 3.8 ou superior

## Atenção — versão do Java e JavaFX

O projeto usa JavaFX. A versão do Java e do JavaFX configuradas no `pom.xml` precisam ser compatíveis entre si.

Se ao compilar ou executar aparecer erro relacionado ao JavaFX ou à versão do Java, abra o `pom.xml` e ajuste as propriedades abaixo para a versão do Java instalada na sua máquina:

```xml
<properties>
    <java.version>21</java.version>
    <javafx.version>21</javafx.version>
</properties>
```

Para verificar a versão do Java instalada:

```bash
java -version
```

## Como compilar

```bash
mvn clean package
```

## Como executar

```bash
mvn javafx:run
```

## Como usar a IDE

1. Digite o código fonte no editor
2. Clique em **Compilar** para executar a análise léxica, sintática e semântica
3. O resultado aparece no painel **OUTPUT** abaixo do editor
4. A **Tabela de Simbolos** é exibida após a Compilação como uma janela flutuante


## Mensagens de saída

- Azul — compilação concluída sem erros
- Amarelo — compilação concluída com avisos
- Vermelho — erro léxico, sintático ou semântico encontrado

## Avisos gerados pelo compilador

- Variável declarada e não usada
- Variável usada sem ter sido inicializada
- Atribuição de float para int com possível perda de precisão
- Comparação entre tipos diferentes

## Erros gerados pelo compilador

- Identificador não declarado
- Identificador declarado mais de uma vez no mesmo escopo
- Identificador usado fora do escopo em que foi declarado
- Atribuição incompatível entre tipos
- Operação inválida entre tipos
- Função chamada com quantidade incorreta de parâmetros
- Parâmetro de tipo incompatível com o esperado pela função

## Tabela de Símbolos

Após a compilação, é aberta automaticamente a **Tabela de Simbolos** (tambem podendo ser acessada via botão) para visualizar os identificadores declarados no programa com as informações abaixo:

| Campo | Descrição |
|---|---|
| Nome | nome do identificador |
| Tipo | int, float, string, char, bool ou void |
| Categoria | VARIAVEL, VETOR, PARAMETRO ou ROTINA |
| Escopo | nível de escopo onde foi declarado (0 = global) |
| Inicializado | se o identificador recebeu um valor |
| Usado | se o identificador foi lido em algum ponto do programa |

## Estrutura do projeto

```
src/
  main/
    java/
      com/minimalide/
        gals/      — analisador léxico, sintático e semântico (gerado pelo WebGALS)
        ide/       — interface gráfica (JavaFX)
```

---

# Guia da Linguagem

## Tipos de dados

```
int       — número inteiro
float     — número real
string    — texto entre aspas duplas
char      — caractere entre aspas simples
bool      — true ou false
```

## Declaração de variáveis

```
let x : int;
let y : float;
let nome : string;
let letra : char;
let ativo : bool;
```

## Declaração com inicialização

```
let x : int = 10;
let y : float = 3.14;
let nome : string = "Felipe";
let letra : char = 'a';
let ativo : bool = true;
```

## Declaração múltipla

```
let a, b, c : int;
```

## Vetores

```
let v[10] : int;
v[0] = 10;
v[1] = 20;
out(v[0]);
```

## Atribuição

```
let x : int;
x = 5;
x = x + 1;
```

## Operadores

```
// aritmeticos
x = a + b;
x = a - b;
x = a * b;
x = a / b;
x = a % b;

// relacionais
a > b
a < b
a >= b
a <= b
a == b
a != b

// logicos
a && b
a || b
!a

// bit a bit
a & b
a | b
a ^ b
```

## Entrada e saída

```
in(x);
out(x);
out(nome);
```

## Desvio condicional

```
// simples
if(x > 0) {
    out(x);
}

// composto
if(x > 0) {
    out(x);
} else {
    out(y);
}
```

## Laços de repetição

```
// while
while(x < 10) {
    x = x + 1;
}

// do while
do {
    x = x + 1;
} while(x < 10);

// for
for(let i : int = 0; i < 10; i++) {
    out(i);
}
```

## Funções

```
// declaracao
fn soma(a : int, b : int) : int {
    ret a + b;
}

// funcao void
fn imprimir(x : int) : void {
    out(x);
}

// chamada
let resultado : int;
resultado = soma(3, 4);
out(resultado);
```

## Exemplo completo

```
fn media(a : float, b : float) : float {
    ret (a + b) / 2;
}

fn ePositivo(x : int) : bool {
    ret x > 0;
}

let x : float = 8.0;
let y : float = 6.0;
let resultado : float;
let positivo : bool;
let n : int;

resultado = media(x, y);
out(resultado);

in(n);
positivo = ePositivo(n);

let soma : int = 0;
for(let i : int = 1; i <= 5; i++) {
    soma = soma + i;
}
out(soma);
```
