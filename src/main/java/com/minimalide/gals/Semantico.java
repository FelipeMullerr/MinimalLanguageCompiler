package com.minimalide.gals;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Semantico implements Constants {

    // verificar isso aqui, nao sei se vai dar certo (gambiarra)
    private String nomeUsoPendente;
    private int posicaoUsoPendente;
    private boolean temUsoPendente = false;

    private String tipoAtual;
    private String nomeFuncaoAtual;
    private String nomeVariavelAtribuicao;
    private final List<String> nomesTemp = new ArrayList<>();
    private final List<Simbolo.Categoria> categoriasTemp = new ArrayList<>();
    private final List<Integer> posicoesTemp = new ArrayList<>();

    private final List<Simbolo> tabelaSimbolos = new ArrayList<>();

    private final Stack<String> pilhaTipos = new Stack<>();

    private final List<String> warnings = new ArrayList<>();

    public void executeAction(int action, Token token) throws SemanticError {
        System.out.println(
            "Ação #" +
                action +
                " | lexema: " +
                token.getLexeme() +
                " | pos: " +
                token.getPosition()
        );

        switch (action) {
            // pega o tipo e insere todos os ids que estavam esperando
            case 1: {
                tipoAtual = token.getLexeme();
                for (int i = 0; i < nomesTemp.size(); i++) {
                    declararSimbolo(
                        nomesTemp.get(i),
                        categoriasTemp.get(i),
                        posicoesTemp.get(i)
                    );
                }
                nomesTemp.clear();
                categoriasTemp.clear();
                posicoesTemp.clear();
                break;
            }
            // variavel simples sendo declarada, guarda pra inserir quando o tipo chegar
            case 2: {
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.VARIAVEL);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // vetor sendo declarado, guarda pra inserir quando o tipo chegar
            case 3: {
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.VETOR);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // tipo de retorno da funcao e void
            case 4: {
                tipoAtual = "void";
                break;
            }
            // nome da funcao, guarda pra usar depois
            case 5: {
                nomeFuncaoAtual = token.getLexeme();
                break;
            }
            // parametro normal de funcao
            case 6: {
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.PARAMETRO);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // parametro vetor de funcao
            case 7: {
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.PARAMETRO);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // variavel do lado esquerdo da atribuicao, verifica se foi declarada
            case 8: {
                String nome = token.getLexeme();
                Simbolo s = verificarDeclaracao(
                    token.getLexeme(),
                    token.getPosition()
                );
                s.usado = true;

                // se possui uso era lado esquerdo de atribuicao, cancela o uso pois nao é leitura e sim escrita
                if (temUsoPendente && nome.equals(nomeUsoPendente)) {
                    temUsoPendente = false;
                }

                nomeVariavelAtribuicao = nome;

                break;
            }
            // fim da atribuicao, checa se os tipos batem e marca a variavel como inicializada
            case 9: {
                if (nomeVariavelAtribuicao != null) {
                    Simbolo variavel = buscarSimbolo(nomeVariavelAtribuicao);
                    if (variavel != null && !pilhaTipos.isEmpty()) {
                        String tipoExpressao = pilhaTipos.pop();
                        verificarTipoAtribuicao(
                            variavel.tipo,
                            tipoExpressao,
                            token.getPosition()
                        );
                        variavel.inicializado = true;
                    }
                    nomeVariavelAtribuicao = null;
                }

                // se tiver uso pendente é pq é leitura (variavel usada)
                if (temUsoPendente) {
                    Simbolo s = buscarSimbolo(nomeUsoPendente);

                    if (s != null) {
                        s.usado = true;

                        if (!s.inicializado) {
                            warnings.add(
                                "Aviso: '" +
                                    s.nome +
                                    "' usado sem inicializacao (posicao " +
                                    posicaoUsoPendente +
                                    ")"
                            );
                        }
                    }

                    temUsoPendente = false;
                }

                break;
            }
            // uso de variavel em expressao, empilha o tipo dela
            case 10: {
                nomeUsoPendente = token.getLexeme();
                posicaoUsoPendente = token.getPosition();
                temUsoPendente = true;

                Simbolo s = verificarDeclaracao(
                    token.getLexeme(),
                    token.getPosition()
                );
                //s.usado = true;

                pilhaTipos.push(s.tipo);
                break;
            }
            // uso de vetor em expressao, empilha o tipo dele
            case 11: {
                Simbolo s = verificarDeclaracao(
                    token.getLexeme(),
                    token.getPosition()
                );
                s.usado = true;

                pilhaTipos.push(s.tipo);
                break;
            }
            // chamada de funcao, empilha o tipo de retorno dela
            case 12: {
                Simbolo s = verificarDeclaracao(
                    token.getLexeme(),
                    token.getPosition()
                );
                s.usado = true;
                pilhaTipos.push(s.tipo);
                break;
            }
            // numero inteiro na expressao
            case 13: {
                pilhaTipos.push("int");
                break;
            }
            // numero float na expressao
            case 14: {
                pilhaTipos.push("float");
                break;
            }
            // string na expressao
            case 15: {
                pilhaTipos.push("string");
                break;
            }
            // char na expressao
            case 16: {
                pilhaTipos.push("char");
                break;
            }
            // true ou false na expressao
            case 17: {
                pilhaTipos.push("bool");
                break;
            }
            // operacao de + ou -, verificar tipos
            case 18: {
                String tipoOp2 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                pilhaTipos.push(
                    verificarTipoAritmetico(tipoOp1, tipoOp2, token)
                );
                break;
            }
            // operacao de * / %, verificar tipos
            case 19: {
                String tipoOp2 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                pilhaTipos.push(
                    verificarTipoAritmetico(tipoOp1, tipoOp2, token)
                );
                break;
            }
            // operacao relacional, resultado e sempre bool
            case 20: {
                String tipoOp2 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                verificarTipoRelacional(tipoOp1, tipoOp2, token);
                pilhaTipos.push("bool");
                break;
            }
            // operacao &&, os dois lados tem que ser bool
            case 21: {
                String tipoOp2 = pilhaTipos.isEmpty()
                    ? "bool"
                    : pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.isEmpty()
                    ? "bool"
                    : pilhaTipos.pop();
                if (!tipoOp1.equals("bool") || !tipoOp2.equals("bool")) {
                    throw new SemanticError(
                        "Operador '&&' requer operandos bool, encontrado: " +
                            tipoOp1 +
                            " e " +
                            tipoOp2,
                        token.getPosition()
                    );
                }
                pilhaTipos.push("bool");
                break;
            }
            // operacao ||, os dois lados tem que ser bool
            case 22: {
                String tipoOp2 = pilhaTipos.isEmpty()
                    ? "bool"
                    : pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.isEmpty()
                    ? "bool"
                    : pilhaTipos.pop();
                if (!tipoOp1.equals("bool") || !tipoOp2.equals("bool")) {
                    throw new SemanticError(
                        "Operador '||' requer operandos bool, encontrado: " +
                            tipoOp1 +
                            " e " +
                            tipoOp2,
                        token.getPosition()
                    );
                }
                pilhaTipos.push("bool");
                break;
            }
            // operacao !, o lado tem que ser bool
            case 23: {
                String tipoOperando = pilhaTipos.isEmpty()
                    ? "bool"
                    : pilhaTipos.pop();
                if (!tipoOperando.equals("bool")) {
                    throw new SemanticError(
                        "Operador '!' requer operando bool, encontrado: " +
                            tipoOperando,
                        token.getPosition()
                    );
                }
                pilhaTipos.push("bool");
                break;
            }
            // menos unario, o lado tem que ser numerico
            case 24: {
                String tipoOperando = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                if (
                    !tipoOperando.equals("int") && !tipoOperando.equals("float")
                ) {
                    throw new SemanticError(
                        "Operador '-' unario requer operando numerico, encontrado: " +
                            tipoOperando,
                        token.getPosition()
                    );
                }
                pilhaTipos.push(tipoOperando);
                break;
            }
            // variavel de controle do for, guarda pra inserir quando o tipo chegar
            case 25: {
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.VARIAVEL);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // fim da declaracao de funcao, insere ela na tabela de simbolos
            case 27: {
                if (nomeFuncaoAtual != null) {
                    if (verificarDuplicata(nomeFuncaoAtual)) {
                        throw new SemanticError(
                            "Funcao '" + nomeFuncaoAtual + "' ja declarada.",
                            token.getPosition()
                        );
                    }
                    Simbolo s = new Simbolo();
                    s.nome = nomeFuncaoAtual;
                    s.tipo = tipoAtual != null ? tipoAtual : "void";
                    s.categoria = Simbolo.Categoria.ROTINA;
                    s.inicializado = true;
                    tabelaSimbolos.add(s);
                    nomeFuncaoAtual = null;
                    tipoAtual = null;
                }
                break;
            }
            // operacao bit a bit, os dois lados tem que ser int
            case 28: {
                String tipoOp2 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                if (!tipoOp1.equals("int") || !tipoOp2.equals("int")) {
                    throw new SemanticError(
                        "Operadores bit a bit requerem operandos int, encontrado: " +
                            tipoOp1 +
                            " e " +
                            tipoOp2,
                        token.getPosition()
                    );
                }
                pilhaTipos.push("int");
                break;
            }
            // operacao de shift, os dois lados tem que ser int
            case 29: {
                String tipoOp2 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                if (!tipoOp1.equals("int") || !tipoOp2.equals("int")) {
                    throw new SemanticError(
                        "Operadores de shift requerem operandos int, encontrado: " +
                            tipoOp1 +
                            " e " +
                            tipoOp2,
                        token.getPosition()
                    );
                }
                pilhaTipos.push("int");
                break;
            }
            // operacao ~, o lado tem que ser int
            case 30: {
                String tipoOperando = pilhaTipos.isEmpty()
                    ? "int"
                    : pilhaTipos.pop();
                if (!tipoOperando.equals("int")) {
                    throw new SemanticError(
                        "Operador '~' requer operando int, encontrado: " +
                            tipoOperando,
                        token.getPosition()
                    );
                }
                pilhaTipos.push("int");
                break;
            }
            default:
                break;
        }
    }

    public void verificarNaoUsados() {
        for (Simbolo s : tabelaSimbolos) {
            if (!s.usado) {
                warnings.add("Aviso: '" + s.nome + "' declarado e nao usado.");
            }
        }
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<Simbolo> getTabelaSimbolos() {
        return tabelaSimbolos;
    }

    private void declararSimbolo(
        String nome,
        Simbolo.Categoria categoria,
        int pos
    ) throws SemanticError {
        if (tipoAtual == null || tipoAtual.isBlank()) {
            throw new SemanticError(
                "Tipo nao definido para declaracao de '" + nome + "'",
                pos
            );
        }
        if (verificarDuplicata(nome)) {
            throw new SemanticError(
                "Identificador '" + nome + "' ja declarado.",
                pos
            );
        }
        Simbolo s = new Simbolo();
        s.nome = nome;
        s.tipo = tipoAtual;
        s.categoria = categoria;
        s.nivelEscopo = 0;
        s.inicializado = false;
        s.usado = false;
        tabelaSimbolos.add(s);
    }

    private boolean verificarDuplicata(String nome) {
        for (Simbolo s : tabelaSimbolos) {
            if (s.nome.equals(nome)) return true;
        }
        return false;
    }

    private Simbolo buscarSimbolo(String nome) {
        for (int i = tabelaSimbolos.size() - 1; i >= 0; i--) {
            if (
                tabelaSimbolos.get(i).nome.equals(nome)
            ) return tabelaSimbolos.get(i);
        }
        return null;
    }

    private Simbolo verificarDeclaracao(String nome, int pos)
        throws SemanticError {
        Simbolo s = buscarSimbolo(nome);
        if (s == null) {
            throw new SemanticError(
                "Identificador '" + nome + "' nao declarado.",
                pos
            );
        }
        return s;
    }

    private String verificarTipoAritmetico(
        String tipoOp1,
        String tipoOp2,
        Token token
    ) throws SemanticError {
        if (
            (tipoOp1.equals("int") || tipoOp1.equals("float")) &&
            (tipoOp2.equals("int") || tipoOp2.equals("float"))
        ) {
            return (tipoOp1.equals("float") || tipoOp2.equals("float"))
                ? "float"
                : "int";
        }
        throw new SemanticError(
            "Operacao aritmetica invalida entre tipos: " +
                tipoOp1 +
                " e " +
                tipoOp2,
            token.getPosition()
        );
    }

    private void verificarTipoRelacional(
        String tipoOp1,
        String tipoOp2,
        Token token
    ) throws SemanticError {
        boolean numericos =
            (tipoOp1.equals("int") || tipoOp1.equals("float")) &&
            (tipoOp2.equals("int") || tipoOp2.equals("float"));
        boolean iguais = tipoOp1.equals(tipoOp2);
        if (!numericos && !iguais) {
            throw new SemanticError(
                "Comparacao invalida entre tipos: " + tipoOp1 + " e " + tipoOp2,
                token.getPosition()
            );
        }
    }

    private void verificarTipoAtribuicao(
        String tipoVariavel,
        String tipoExpressao,
        int pos
    ) throws SemanticError {
        if (tipoVariavel.equals(tipoExpressao)) return;
        if (tipoVariavel.equals("float") && tipoExpressao.equals("int")) return;
        throw new SemanticError(
            "Atribuicao incompativel: variavel '" +
                nomeVariavelAtribuicao +
                "' e do tipo " +
                tipoVariavel +
                " mas recebe " +
                tipoExpressao,
            pos
        );
    }
}
