package com.minimalide.gals;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Semantico implements Constants {

    private String tipoAtual;
    private String nomeFuncaoAtual;
    private String nomeVariavelAtribuicao;
    private String nomeFuncaoChamada;
    private boolean declarandoParametros = false;
    private int contadorEscopo = 0;
    private int tamanhoPilhaAntesChamada = 0;
    private String nomeUltimaVariavel;
    private boolean declarandoForInit = false;
    private int tamanhoVetorTemp = 0;
    private String nomeVetorEsquerdo = null;
    private String operadorAtual = null;
    private String operadorPrincipal = null;
    private String indiceVetorProcessado = null;
    private String nomeVetorProcessado = null;
    private boolean indiceNoAcumulador = false;
    private String nomeVetorAtual = null;
    private boolean dentroIndiceVetor = false;

    private GeradorCodigo gerador;
    public void setGerador(GeradorCodigo g) { this.gerador = g; }
    private final List<String> nomesTemp = new ArrayList<>();
    private final List<Simbolo.Categoria> categoriasTemp = new ArrayList<>();
    private final List<Integer> posicoesTemp = new ArrayList<>();
    private final List<String> nomesUsoExpressao = new ArrayList<>();
    private final List<Integer> posicoesUsoExpressao = new ArrayList<>();
    private final List<String> tiposArgsAtual = new ArrayList<>();
    private final Stack<Integer> pilhaEscopo = new Stack<>();
    { pilhaEscopo.push(0); }
    private final List<Simbolo> tabelaSimbolos = new ArrayList<>();
    private final Stack<String> pilhaTipos = new Stack<>();
    private final Stack<String> pilhaValores = new Stack<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> nomesUltimaDeclaracao = new ArrayList<>();

    private final Stack<String> pilhaTempsVetor = new Stack<>();

    public void executeAction(int action, Token token) throws SemanticError {
        System.out.println("Ação #" + action + " | lexema: " + token.getLexeme() + " | pos: " + token.getPosition());

        switch (action) {
            // captura o tipo da declaracao e insere todos os ids pendentes na tabela de simbolos
            case 1: {
                tipoAtual = token.getLexeme();
                for (int i = 0; i < nomesTemp.size(); i++) {
                    if (declarandoParametros) {
                        pilhaEscopo.push(contadorEscopo + 1);
                        declararSimbolo(nomesTemp.get(i), categoriasTemp.get(i), posicoesTemp.get(i));
                        pilhaEscopo.pop();
                        Simbolo funcao = buscarSimboloGlobal(nomeFuncaoAtual);
                        if (funcao != null) funcao.tiposParametros.add(tipoAtual);
                    } else if (declarandoForInit) {
                        pilhaEscopo.push(contadorEscopo + 1);
                        declararSimbolo(nomesTemp.get(i), categoriasTemp.get(i), posicoesTemp.get(i));
                        pilhaEscopo.pop();
                    } else {
                        declararSimbolo(nomesTemp.get(i), categoriasTemp.get(i), posicoesTemp.get(i));
                    }
                }
                if (!declarandoParametros && nomeFuncaoAtual != null) {
                    Simbolo funcao = buscarSimboloGlobal(nomeFuncaoAtual);
                    if (funcao != null) funcao.tipo = tipoAtual;
                }
                declarandoForInit = false;
                declarandoParametros = false;
                nomesUltimaDeclaracao.clear();
                nomesUltimaDeclaracao.addAll(nomesTemp);
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
            // captura o tipo void como retorno da funcao e atualiza na tabela de simbolos
            case 4: {
                tipoAtual = "void";
                if (nomeFuncaoAtual != null) {
                    Simbolo funcao = buscarSimboloGlobal(nomeFuncaoAtual);
                    if (funcao != null) funcao.tipo = "void";
                }
                break;
            }
            // nome da funcao, insere ja na tabela com tipo provisorio
            case 5: {
                nomeFuncaoAtual = token.getLexeme();
                if (buscarSimboloGlobal(nomeFuncaoAtual) != null) {
                    throw new SemanticError("Funcao '" + nomeFuncaoAtual + "' ja declarada.", token.getPosition());
                }
                Simbolo s = new Simbolo();
                s.nome = nomeFuncaoAtual;
                s.tipo = "void";
                s.categoria = Simbolo.Categoria.ROTINA;
                s.nivelEscopo = 0;
                s.inicializado = true;
                s.usado = false;
                tabelaSimbolos.add(s);
                break;
            }
            // guarda o parametro simples da funcao nas listas temp e ativa a flag de parametros
            case 6: {
                declarandoParametros = true;
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.PARAMETRO);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // guarda o parametro vetor da funcao na lista temp e ativa a flag de parametros
            case 7: {
                declarandoParametros = true;
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.PARAMETRO);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // remove o identificador da lista de usos pois confirmamos que é o lado esquerdo da atribuicao
            // o case 10 ja tinha adicionado ele na lista, mas escrita nao e leitura entao removemos
            case 8: {
                String nomeParaRemover = (nomeVetorEsquerdo != null) ? nomeVetorEsquerdo : nomeUltimaVariavel;
                if (!nomesUsoExpressao.isEmpty()) {
                    int idx = nomesUsoExpressao.lastIndexOf(nomeParaRemover);
                    if (idx >= 0) {
                        nomesUsoExpressao.remove(idx);
                        posicoesUsoExpressao.remove(idx);
                    }
                }
                if (nomeVetorEsquerdo == null && !pilhaValores.isEmpty()) {
                    pilhaValores.pop();
                }
                nomeVariavelAtribuicao = nomeParaRemover;
                verificarDeclaracao(nomeParaRemover, token.getPosition());

                if (nomeVetorEsquerdo != null && gerador != null) {
                    if (indiceNoAcumulador) {
                        gerador.gerarText("STO " + GeradorCodigo.TEMP_ATRIB);
                        indiceNoAcumulador = false;
                    } else if (indiceVetorProcessado != null) {
                        if (indiceVetorProcessado.matches("[+-]?\\d+")) {
                            gerador.gerarText("LDI " + indiceVetorProcessado);
                        } else {
                            gerador.gerarText("LD " + indiceVetorProcessado);
                        }
                        gerador.gerarText("STO " + GeradorCodigo.TEMP_ATRIB);
                    }
                    indiceVetorProcessado = null;
                    nomeVetorProcessado = null;
                }

                nomeVetorEsquerdo = null;
                break;
            }
            // processa o fim da atribuicao, verifica tipos e marca a variavel como inicializada
            case 9: {
                for (int i = 0; i < nomesUsoExpressao.size(); i++) {
                    String nome = nomesUsoExpressao.get(i);
                    int pos = posicoesUsoExpressao.get(i);
                    Simbolo s = buscarSimbolo(nome);
                    if (s != null) {
                        s.usado = true;
                        if (!s.inicializado) {
                            warnings.add("Aviso: '" + s.nome + "' usado sem inicializacao (posicao " + pos + ")");
                        }
                    }
                }
                nomesUsoExpressao.clear();
                posicoesUsoExpressao.clear();

                String nomeDestino = nomeVariavelAtribuicao;
                if (nomeVariavelAtribuicao != null) {
                    Simbolo variavel = buscarSimbolo(nomeVariavelAtribuicao);
                    if (variavel != null && !pilhaTipos.isEmpty()) {
                        String tipoExpressao = pilhaTipos.pop();
                        verificarTipoAtribuicao(variavel.tipo, tipoExpressao, token.getPosition());
                        variavel.inicializado = true;
                    }
                    nomeVariavelAtribuicao = null;
                }

                if (gerador != null && nomeDestino != null) {
                    Simbolo simDestino = buscarSimbolo(nomeDestino);
                    boolean destinoEhVetor = simDestino != null && simDestino.categoria == Simbolo.Categoria.VETOR;

                    if (destinoEhVetor) {
                        if (pilhaTempsVetor.size() >= 2) {
                            // dois vetores com operacao entre eles
                            String temp2 = pilhaTempsVetor.pop();
                            String temp1 = pilhaTempsVetor.pop();
                            gerador.gerarText("LD " + temp1);
                            if ("-".equals(operadorPrincipal)) {
                                gerador.gerarText("SUB " + temp2);
                            } else {
                                gerador.gerarText("ADD " + temp2);
                            }
                            gerador.gerarText("STO " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_ATRIB);
                            gerador.gerarText("STO " + GeradorCodigo.INDR);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("STOV " + nomeDestino);
                            gerador.resetTemps();
                            operadorPrincipal = null;
                        } else if (!pilhaTempsVetor.isEmpty()) {
                            // um vetor no lado direito
                            String temp = pilhaTempsVetor.pop();
                            gerador.gerarText("STO" + temp);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_ATRIB);
                            gerador.gerarText("STO " + GeradorCodigo.INDR);
                            gerador.gerarText("LD " + temp);
                            gerador.gerarText("STOV " + nomeDestino);
                            gerador.resetTemps();
                        } else if (indiceVetorProcessado != null && nomeVetorProcessado != null) {
                            // vetor dois dois lados (um apenas), porem o indice nao foi processado no case 42
                            if (indiceVetorProcessado.matches("[+-]?\\d+")) {
                                gerador.gerarText("LDI " + indiceVetorProcessado);
                            } else {
                                gerador.gerarText("LD " + indiceVetorProcessado);
                            }
                            gerador.gerarText("STO " + GeradorCodigo.INDR);
                            gerador.gerarText("LDV " + nomeVetorProcessado);
                            indiceVetorProcessado = null;
                            nomeVetorProcessado = null;
                            gerador.gerarText("STO " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_ATRIB);
                            gerador.gerarText("STO " + GeradorCodigo.INDR);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("STOV " + nomeDestino);
                            gerador.resetTemps();
                        } else if (indiceNoAcumulador && nomeVetorProcessado != null) {
                            // vetor com indice que esta no acumulador (ja calculado)
                            gerador.gerarText("STO " + GeradorCodigo.INDR);
                            gerador.gerarText("LDV " + nomeVetorProcessado);
                            indiceNoAcumulador = false;
                            nomeVetorProcessado = null;
                            gerador.gerarText("STO " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_ATRIB);
                            gerador.gerarText("STO " + GeradorCodigo.INDR);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("STOV " + nomeDestino);
                            gerador.resetTemps();
                        } else {
                            // lado direito expressao simples ou aritmetica sem vetor
                            String valor = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                            if (valor != null) {
                                if (valor.matches("[+-]?\\d+")) {
                                    gerador.gerarText("LDI " + valor);
                                } else {
                                    gerador.gerarText("LD " + valor);
                                }
                            }
                            gerador.gerarText("STO " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_ATRIB);
                            gerador.gerarText("STO " + GeradorCodigo.INDR);
                            gerador.gerarText("LD " + GeradorCodigo.TEMP_OP1);
                            gerador.gerarText("STOV " + nomeDestino);
                            gerador.resetTemps();
                        }
                    } else if (pilhaTempsVetor.size() >= 2) {
                        // destino variavel, dois vetores com operacao
                        String temp2 = pilhaTempsVetor.pop();
                        String temp1 = pilhaTempsVetor.pop();
                        gerador.gerarText("LD " + temp1);
                        if ("-".equals(operadorPrincipal)) {
                            gerador.gerarText("SUB " + temp2);
                        } else {
                            gerador.gerarText("ADD " + temp2);
                        }
                        gerador.gerarText("STO " + nomeDestino);
                        gerador.resetTemps();
                        operadorPrincipal = null;
                    } else if (!pilhaTempsVetor.isEmpty()) {
                        // destino variavel, um vetor no lado direito
                        String temp = pilhaTempsVetor.pop();
                        //gerador.gerarText("LD " + temp);
                        gerador.gerarText("STO " + nomeDestino);
                        gerador.resetTemps();
                        operadorPrincipal = null;
                    } else if (indiceVetorProcessado != null && nomeVetorProcessado != null) {
                        // destino variavel, um vetor lado direito e indice nao esta no acc
                        if (indiceVetorProcessado.matches("[+-]?\\d+")) {
                            gerador.gerarText("LDI " + indiceVetorProcessado);
                        } else {
                            gerador.gerarText("LD " + indiceVetorProcessado);
                        }
                        gerador.gerarText("STO " + GeradorCodigo.INDR);
                        gerador.gerarText("LDV " + nomeVetorProcessado);
                        gerador.gerarText("STO " + nomeDestino);
                        indiceVetorProcessado = null;
                        nomeVetorProcessado = null;
                    } else if (indiceNoAcumulador && nomeVetorProcessado != null) {
                        // destino variavel, um vetor lado direito com indice no acc
                        gerador.gerarText("STO " + GeradorCodigo.INDR);
                        gerador.gerarText("LDV " + nomeVetorProcessado);
                        gerador.gerarText("STO " + nomeDestino);
                        indiceNoAcumulador = false;
                        nomeVetorProcessado = null;
                    } else {
                        if (pilhaValores.isEmpty()) {
                            // destino variavel e lado direito variavel
                            gerador.gerarText("STO " + nomeDestino);
                        } else {
                            String valor = pilhaValores.pop();
                            if (valor.matches("[+-]?\\d+")) {
                                gerador.gerarText("LDI " + valor);
                            } else {
                                gerador.gerarText("LD " + valor);
                            }
                            gerador.gerarText("STO " + nomeDestino);
                        }
                    }
                }
                break;
            }
            // guarda o identificador na lista de usos pendentes e empilha o tipo
            // nao sabemos ainda se o identificador esta no lado esquerdo ou direita de uma atribuicao
            // o case 8 remove o uso pendente se for lado esquerdo, o case 9 ou 35 processa se for lado direito
            case 10: {
                nomeUltimaVariavel = token.getLexeme();
                Simbolo s = verificarDeclaracao(token.getLexeme(), token.getPosition());
                nomesUsoExpressao.add(token.getLexeme());
                posicoesUsoExpressao.add(token.getPosition());
                pilhaTipos.push(s.tipo);
                pilhaValores.push(token.getLexeme());
                break;
            }
            // verifica o uso de um vetor com indice em expressao e empilha o tipo dele
            case 11: {
                dentroIndiceVetor = true;
                if (nomeVariavelAtribuicao == null) {
                    nomeVetorEsquerdo = token.getLexeme();
                } else {
                    nomeVetorEsquerdo = null;
                }
                nomeUltimaVariavel = token.getLexeme();
                nomeVetorAtual = token.getLexeme();
                Simbolo s = verificarDeclaracao(nomeUltimaVariavel, token.getPosition());
                nomesUsoExpressao.add(token.getLexeme());
                posicoesUsoExpressao.add(token.getPosition());
                pilhaTipos.push(s.tipo);
                break;
            }
            // verifica se a funcao foi declarada e guarda o nome pra verificar parametros no case 38
            // e empilha o tipo de retorno pra ser usado na expressao que envolve a chamada
            case 12: {
                Simbolo s = verificarDeclaracao(token.getLexeme(), token.getPosition());
                s.usado = true;
                nomeFuncaoChamada = token.getLexeme();
                tiposArgsAtual.clear();
                tamanhoPilhaAntesChamada = pilhaTipos.size();
                pilhaTipos.push(s.tipo);
                break;
            }
            // empilha o tipo do literal encontrado na expressao
            case 13: {
                pilhaTipos.push("int");
                pilhaValores.push(token.getLexeme());
                break;
            }
            case 14: {
                pilhaTipos.push("float");
                break;
            }
            case 15: {
                pilhaTipos.push("string");
                break;
            }
            case 16: {
                pilhaTipos.push("char");
                break;
            }
            case 17: {
                pilhaTipos.push("bool");
                break;
            }
            // verifica compatibilidade dos operandos para + ou - e empilha o tipo resultante
            case 18: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                pilhaTipos.push(verificarTipoExpressao(tipoOp1, tipoOp2, TabelaSemantica.SUM, token));

                if (gerador != null) {
                    String op2 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    String op1 = pilhaValores.isEmpty() ? null : pilhaValores.pop();

                    if (op1 != null) {
                        if (op1.matches("[+-]?\\d+")) {
                            gerador.gerarText("LDI " + op1);
                        } else {
                            gerador.gerarText("LD " + op1);
                        }
                    }
                    if (op2 != null) {
                        boolean literal = op2.matches("[+-]?\\d+");
                        if ("+".equals(operadorAtual)) {
                            gerador.gerarText(literal ? "ADDI " + op2 : "ADD " + op2);
                        } else {
                            gerador.gerarText(literal ? "SUBI " + op2 : "SUB " + op2);
                        }
                    }
                    operadorAtual = null;
                }
                break;
            }
            // verifica compatibilidade dos operandos para * / % e empilha o tipo resultante
            case 19: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                pilhaTipos.push(verificarTipoExpressao(tipoOp1, tipoOp2, TabelaSemantica.MUL, token));
                break;
            }
            // verifica compatibilidade dos operandos para operadores relacionais e empilha bool
            case 20: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                pilhaTipos.push(verificarTipoExpressao(tipoOp1, tipoOp2, TabelaSemantica.REL, token));
                break;
            }
            // verifica se os dois operandos do && sao bool e empilha bool
            case 21: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                if (!tipoOp1.equals("bool") || !tipoOp2.equals("bool")) {
                    throw new SemanticError("Operador '&&' requer operandos bool, encontrado: " + tipoOp1 + " e " + tipoOp2,token.getPosition());
                }
                pilhaTipos.push("bool");
                break;
            }
            // verifica se os dois operandos do || sao bool e empilha bool
            case 22: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                if (!tipoOp1.equals("bool") || !tipoOp2.equals("bool")) {
                    throw new SemanticError("Operador '||' requer operandos bool, encontrado: " + tipoOp1 + " e " + tipoOp2,token.getPosition());
                }
                pilhaTipos.push("bool");
                break;
            }
            // verifica se o operando do ! e bool e empilha bool
            case 23: {
                String tipoOperando = pilhaTipos.pop();
                if (!tipoOperando.equals("bool")) {
                    throw new SemanticError("Operador '!' requer operando bool, encontrado: " + tipoOperando,token.getPosition());
                }
                pilhaTipos.push("bool");
                break;
            }
            // menos unario, o lado tem que ser numerico
            case 24: {
                String tipoOperando = pilhaTipos.pop();
                if (!tipoOperando.equals("int") && !tipoOperando.equals("float")) {
                    throw new SemanticError("Operador '-' unario requer operando numerico, encontrado: " + tipoOperando,token.getPosition());
                }
                pilhaTipos.push(tipoOperando);
                break;
            }
            // variavel de controle do for, guarda pra inserir quando o tipo chegar
            case 25: {
                declarandoForInit = true;
                nomesTemp.add(token.getLexeme());
                categoriasTemp.add(Simbolo.Categoria.VARIAVEL);
                posicoesTemp.add(token.getPosition());
                break;
            }
            // fim da declaracao da funcao, reseta nome
            case 27: {
                nomeFuncaoAtual = null;
                break;
            }
            // verifica se os dois operandos do OR bit a bit sao int e empilha int
            case 28: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                if (!tipoOp1.equals("int") || !tipoOp2.equals("int")) {
                    throw new SemanticError("Operadores bit a bit requerem operandos int, encontrado: " + tipoOp1 + " e " + tipoOp2,token.getPosition());
                }
                pilhaTipos.push("int");

                if(gerador != null) {
                    String op2 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    String op1 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    if (op1 != null) {
                        if (op1.matches("[+-]?\\d+")) gerador.gerarText("LDI " + op1);
                        else gerador.gerarText("LD " + op1);
                    }
                    if (op2 != null) {
                        boolean literal = op2.matches("[+-]?\\d+");
                        gerador.gerarText(literal ? "ORI " + op2 : "OR " + op2);
                    }
                    operadorAtual = null;
                }
                break;
            }
            // verifica se os dois operandos do shifts sao int e empilha int
            case 29: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                if (!tipoOp1.equals("int") || !tipoOp2.equals("int")) {
                    throw new SemanticError("Operadores de shift requerem operandos int, encontrado: " + tipoOp1 + " e " + tipoOp2,token.getPosition());
                }
                pilhaTipos.push("int");
                if (gerador != null) {
                    String op2 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    String op1 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    if (op1 != null) {
                        if (op1.matches("[+-]?\\d+")) gerador.gerarText("LDI " + op1);
                        else gerador.gerarText("LD " + op1);
                    }
                    if (op2 != null) {
                        if ("<<".equals(operadorAtual)) {
                            gerador.gerarText("SLL " + op2);
                        } else {
                            gerador.gerarText("SRL " + op2);
                        }
                    }
                    operadorAtual = null;
                }
                break;
            }
            // verifica se o operando do NOT bit a bit e int e empilha int
            case 30: {
                String tipoOperando = pilhaTipos.pop();
                if (!tipoOperando.equals("int")) {
                    throw new SemanticError("Operador '~' requer operando int, encontrado: " + tipoOperando,token.getPosition());
                }
                pilhaTipos.push("int");
                if (gerador != null) {
                    // como NOT usa apenas um operador, o valor ja esta na pilha de valores
                    if (!pilhaValores.isEmpty()) {
                        String op = pilhaValores.pop();
                        if (op.matches("[+-]?\\d+")) gerador.gerarText("LDI " + op);
                        else gerador.gerarText("LD " + op);
                    }
                    gerador.gerarText("NOT");
                }
                break;
            }
            // entrou no in, limpa a lista pra coletar as variaveis
            case 31: {
                nomesUsoExpressao.clear();
                posicoesUsoExpressao.clear();
                pilhaValores.clear();
                break;
            }
            // fim do in, marca todas as variaveis encontradas como inicializadas e usadas
            case 32: {
                if (gerador != null) {
                    if (indiceVetorProcessado != null && nomeVetorAtual != null) {
                        gerador.gerarText("LD $in_port");
                        gerador.gerarText("STO " + GeradorCodigo.TEMP_OP1);
                        if (indiceVetorProcessado.matches("[+-]?\\d+")) {
                            gerador.gerarText("LDI " + indiceVetorProcessado);
                        } else {
                            gerador.gerarText("LD " + indiceVetorProcessado);
                        }
                        gerador.gerarText("STO " + GeradorCodigo.INDR);
                        gerador.gerarText("LD " + GeradorCodigo.TEMP_OP1);
                        gerador.gerarText("STOV " + nomeVetorAtual);
                        indiceVetorProcessado = null;
                        nomeVetorProcessado = null;
                    } else if (indiceNoAcumulador && nomeVetorAtual != null) {
                        gerador.gerarText("LD $in_port");
                        gerador.gerarText("STO " + GeradorCodigo.TEMP_OP1);
                        gerador.gerarText("STO " + GeradorCodigo.INDR);
                        gerador.gerarText("LD " + GeradorCodigo.TEMP_OP1);
                        gerador.gerarText("STOV " + nomeVetorAtual);
                        indiceNoAcumulador = false;
                    } else {
                        for (String nome : nomesUsoExpressao) {
                            Simbolo s = buscarSimbolo(nome);
                            if (s != null && s.categoria != Simbolo.Categoria.VETOR) {
                                gerador.gerarText("LD $in_port");
                                gerador.gerarText("STO " + nome);
                            }
                        }
                    }
                }
                for (String nome : nomesUsoExpressao) {
                    Simbolo s = buscarSimbolo(nome);
                    if (s != null) {
                        s.inicializado = true;
                        //s.usado = true;
                    }
                }
                nomesUsoExpressao.clear();
                posicoesUsoExpressao.clear();
                pilhaValores.clear();
                break;
            }
            // verifica se os dois operandos do XOR bit a bit sao int e empilha int
            case 33: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                if (!tipoOp1.equals("int") || !tipoOp2.equals("int")) {
                    throw new SemanticError("Operadores bit a bit requerem operandos int, encontrado: " + tipoOp1 + " e " + tipoOp2,token.getPosition());
                }
                pilhaTipos.push("int");

                if (gerador != null) {
                    String op2 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    String op1 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    if (op1 != null) {
                        if (op1.matches("[+-]?\\d+")) gerador.gerarText("LDI " + op1);
                        else gerador.gerarText("LD " + op1);
                    }
                    if (op2 != null) {
                        boolean literal = op2.matches("[+-]?\\d+");
                        gerador.gerarText(literal ? "XORI " + op2 : "XOR " + op2);
                    }
                    operadorAtual = null;
                }
                break;
            }
            // operacao AND bit a bit, os dois lados tem que ser int
            case 34: {
                String tipoOp2 = pilhaTipos.pop();
                String tipoOp1 = pilhaTipos.pop();
                if (!tipoOp1.equals("int") || !tipoOp2.equals("int")) {
                    throw new SemanticError("Operadores bit a bit requerem operandos int, encontrado: " + tipoOp1 + " e " + tipoOp2,token.getPosition());
                }
                pilhaTipos.push("int");
                if (gerador != null) {
                    String op2 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    String op1 = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                    if (op1 != null) {
                        if (op1.matches("[+-]?\\d+")) gerador.gerarText("LDI " + op1);
                        else gerador.gerarText("LD " + op1);
                    }
                    if (op2 != null) {
                        boolean literal = op2.matches("[+-]?\\d+");
                        gerador.gerarText(literal ? "ANDI " + op2 : "AND " + op2);
                    }
                    operadorAtual = null;
                }
                break;
            }
            // processa todos os usos de uma expressao que nao é atribuicao, no caso ususo como if, while, out, return, etc
            // tudo que ficou na lista nomesUsoExpressao é realmente leitura e precisa ser marcado como usado
            // avisa tambem se algum indentificador foi lido sem ter sido inicializado antes
            case 35: {
                for (int i = 0; i < nomesUsoExpressao.size(); i++) {
                    String nome = nomesUsoExpressao.get(i);
                    int pos = posicoesUsoExpressao.get(i);
                    Simbolo s = verificarDeclaracao(nome, pos);
                    s.usado = true;
                    if (!s.inicializado) {
                        String aviso = "Aviso: '" + s.nome + "' usado sem inicializacao";
                        boolean jaTemAviso = warnings.stream().anyMatch(w -> w.contains("'" + s.nome + "' usado sem inicializacao"));
                        if (!jaTemAviso) {
                            warnings.add(aviso + " (posicao " + pos + ")");
                        }
                    }
                }
                nomesUsoExpressao.clear();
                posicoesUsoExpressao.clear();
                break;
            }
            // abre novo escopo, empilha o novo nivel do escopo na pilha
            case 36: {
                contadorEscopo++;
                pilhaEscopo.push(contadorEscopo);
                nomeFuncaoAtual = null;
                break;
            }
            // fecha escopo, avisa variaveis declaradas e nao usadas dentro dele
            case 37: {
                int escopoAtual = pilhaEscopo.peek();
                for (Simbolo s : tabelaSimbolos) {
                    if (s.nivelEscopo == escopoAtual && !s.usado) {
                        warnings.add("Aviso: '" + s.nome + "' declarado e nao usado.");
                    }
                }
                pilhaEscopo.pop();
                break;
            }
            // fim da chamada de funcao, aqui é verificada a quantidade e tipos dos argumentos da funcao e dos argumentos passados
            // usa a tabela semantica pra decidir se e OK, WAR (aviso de precisao) ou ERR (incompativel)
            case 38: {
                if (nomeFuncaoChamada != null) {
                    Simbolo funcao = buscarSimboloGlobal(nomeFuncaoChamada);
                    if (funcao != null) {
                        if (tiposArgsAtual.size() != funcao.tiposParametros.size()) {
                            throw new SemanticError("Funcao '" + nomeFuncaoChamada + "' esperava " + funcao.tiposParametros.size() +" parametros mas recebeu " + tiposArgsAtual.size() + ".",token.getPosition());
                        }
                        for (int i = 0; i < tiposArgsAtual.size(); i++) {
                            String tipoEsperado = funcao.tiposParametros.get(i);
                            String tipoRecebido = tiposArgsAtual.get(i);
                            int tv = TabelaSemantica.tipoParaIndice(tipoEsperado);
                            int te = TabelaSemantica.tipoParaIndice(tipoRecebido);
                            int resultado = TabelaSemantica.atribType(tv, te);
                            if (resultado == TabelaSemantica.WAR) {
                                warnings.add("Aviso: parametro " + (i+1) + " da funcao '" + nomeFuncaoChamada + "' esperava " + tipoEsperado + " mas recebeu " + tipoRecebido + ".");
                            } else if (resultado == TabelaSemantica.ERR) {
                                throw new SemanticError("Parametro " + (i+1) + " da funcao '" + nomeFuncaoChamada +"' esperava " + tipoEsperado + " mas recebeu " + tipoRecebido + ".",token.getPosition());
                            }
                        }
                    }
                    nomeFuncaoChamada = null;
                    tiposArgsAtual.clear();
                }
                break;
            }
            // apos a declaracao de uma funcao, ao final da lista de argumentos, captura os tipos de todos os argumentos empilhados desde o inicio da chamada
            // usa tamanhoPilhaAntesChamada pra saber quantos argumentos foram passados
            // desempilha, guarda na ordem correta em tiposArgsAtual, e reempilha
            case 39: {
                if (nomeFuncaoChamada != null) {
                    tiposArgsAtual.clear();
                    int qtdArgs = pilhaTipos.size() - tamanhoPilhaAntesChamada - 1;
                    List<String> temp = new ArrayList<>();
                    for (int i = 0; i < qtdArgs; i++) {
                        temp.add(0, pilhaTipos.pop());
                    }
                    tiposArgsAtual.addAll(temp);
                    for (String t : tiposArgsAtual) {
                        pilhaTipos.push(t);
                    }
                }
                break;
            }
            // fim de declaracao com inicializacao, verifica tipo e marca os identificadores como inicializados
            case 40: {
                int escopoAtual = pilhaEscopo.peek();
                String tipoExpressao = pilhaTipos.isEmpty() ? null : pilhaTipos.pop();
                String valorExpressao = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                for (Simbolo s : tabelaSimbolos) {
                    if (nomesUltimaDeclaracao.contains(s.nome) &&
                        (s.nivelEscopo == escopoAtual || s.nivelEscopo == contadorEscopo + 1)) {
                        if (tipoExpressao != null) {
                            nomeVariavelAtribuicao = s.nome;
                            verificarTipoAtribuicao(s.tipo, tipoExpressao, token.getPosition());
                        }
                        if (gerador != null && valorExpressao != null) {
                            if (valorExpressao.matches("[+-]?\\d+")) {
                                gerador.gerarText("LDI " + valorExpressao);
                            } else {
                                gerador.gerarText("LD " + valorExpressao);
                            }
                            gerador.gerarText("STO " + s.nome);
                        }
                        s.inicializado = true;
                    }
                }
                nomeVariavelAtribuicao = null;
                nomesUltimaDeclaracao.clear();
                break;
            }
            case 41: {
                tamanhoVetorTemp = Integer.parseInt(token.getLexeme());
                break;
            }
            case 42: {
                // indice do vetor foi calculado e é salvo os valores dos vetores nos temporarios (1000 e 1001)
                // dps de resolvido o indice, ele armazena no $indr, da load no vetor e salva no temporario
                dentroIndiceVetor = false;
                indiceVetorProcessado = pilhaValores.isEmpty() ? null : pilhaValores.pop();
                nomeVetorProcessado = nomeVetorAtual;
                indiceNoAcumulador = (indiceVetorProcessado == null);

                if (gerador != null && nomeVetorEsquerdo == null) {
                    // se indice esta na pilha, gera o LDI ou LD para o indice, caso nao
                    // apenas gera o STO $indr pois o indice ja ta no acc
                    if (!indiceNoAcumulador) {
                        if (indiceVetorProcessado.matches("[+-]?\\d+")) {
                            gerador.gerarText("LDI " + indiceVetorProcessado);
                        } else {
                            gerador.gerarText("LD " + indiceVetorProcessado);
                        }
                    }
                    gerador.gerarText("STO " + GeradorCodigo.INDR);
                    gerador.gerarText("LDV " + nomeVetorProcessado);
                    String temp = gerador.getTemp();
                    gerador.gerarText("STO " + temp);
                    pilhaTempsVetor.push(temp);
                    indiceVetorProcessado = null;
                    nomeVetorProcessado = null;
                    indiceNoAcumulador = false;
                }
                break;
            }
            case 43: {
                operadorAtual = token.getLexeme();
                if (pilhaTempsVetor.size() == 1 && !dentroIndiceVetor) {
                    operadorPrincipal = token.getLexeme();
                }
                break;
            }
            case 44: {
                if (gerador != null) {
                    if (!pilhaTempsVetor.isEmpty()) {
                        String temp = pilhaTempsVetor.pop();
                        gerador.gerarText("LD " + temp);
                        gerador.resetTemps();
                        gerador.gerarText("STO $out_port");
                    } else if (indiceVetorProcessado != null && nomeVetorProcessado != null) {
                        if (indiceVetorProcessado.matches("[+-]?\\d+")) {
                            gerador.gerarText("LDI " + indiceVetorProcessado);
                        } else {
                            gerador.gerarText("LD " + indiceVetorProcessado);
                        }
                        gerador.gerarText("STO " + GeradorCodigo.INDR);
                        gerador.gerarText("LDV " + nomeVetorProcessado);
                        gerador.gerarText("STO $out_port");
                        indiceVetorProcessado = null;
                        nomeVetorProcessado = null;
                    } else if (indiceNoAcumulador && nomeVetorProcessado != null) {
                        gerador.gerarText("STO " + GeradorCodigo.INDR);
                        gerador.gerarText("LDV " + nomeVetorProcessado);
                        gerador.gerarText("STO $out_port");
                        indiceNoAcumulador = false;
                        nomeVetorProcessado = null;
                    } else if (!pilhaValores.isEmpty()) {
                        String valor = pilhaValores.pop();
                        if (valor.matches("[+-]?\\d+")) {
                            gerador.gerarText("LDI " + valor);
                        } else {
                            gerador.gerarText("LD " + valor);
                        }
                        gerador.gerarText("STO $out_port");
                    } else {
                        gerador.gerarText("STO $out_port");
                    }
                }
                break;
            }
            case 45: {
                operadorAtual = token.getLexeme();
                break;
            }
            case 46: {
                operadorAtual = token.getLexeme();
                break;
            }
            default:
                break;
        }
    }

    // verifica todos os simbolos do escopo global que nao foram usados e adiciona aviso
    public void verificarNaoUsados() {
        for (Simbolo s : tabelaSimbolos) {
            if (s.nivelEscopo == 0 && !s.usado) {
                warnings.add("Aviso: '" + s.nome + "' declarado e nao usado.");
            }
        }
    }

    public List<String> getWarnings() { return warnings; }

    public List<Simbolo> getTabelaSimbolos() { return tabelaSimbolos; }

    // declara um simbolo no escopo atual verificando unicidade e tipo definido
    private void declararSimbolo(String nome, Simbolo.Categoria categoria, int pos) throws SemanticError {
        if (tipoAtual == null || tipoAtual.isBlank()) {
            throw new SemanticError("Tipo nao definido para declaracao de '" + nome + "'", pos);
        }
        int escopoAtual = pilhaEscopo.peek();
        for (Simbolo s : tabelaSimbolos) {
            if (s.nome.equals(nome) && s.nivelEscopo == escopoAtual) {
                throw new SemanticError("Identificador '" + nome + "' ja declarado neste escopo (" + escopoAtual + ")", pos);
            }
        }
        Simbolo s = new Simbolo();
        s.nome = nome;
        s.tipo = tipoAtual;
        s.categoria = categoria;
        s.nivelEscopo = escopoAtual;
        s.inicializado = (categoria == Simbolo.Categoria.PARAMETRO || categoria == Simbolo.Categoria.ROTINA);
        s.usado = false;

        if (categoria == Simbolo.Categoria.VETOR) {
            s.tamanhoVetor = tamanhoVetorTemp;
            tamanhoVetorTemp = 0;
        }
        tabelaSimbolos.add(s);
    }

    // busca um simbolo pelo nome considerando apenas os escopos visiveis na pilha atual
    private Simbolo buscarSimbolo(String nome) {
        for (int i = tabelaSimbolos.size() - 1; i >= 0; i--) {
            Simbolo s = tabelaSimbolos.get(i);
            if (s.nome.equals(nome) && (pilhaEscopo.contains(s.nivelEscopo) || s.nivelEscopo == contadorEscopo + 1)) {
                return s;
            }
        }
        return null;
    }

    // busca um simbolo pelo nome apenas no escopo global (escopo 0)
    private Simbolo buscarSimboloGlobal(String nome) {
        for (int i = tabelaSimbolos.size() - 1; i >= 0; i--) {
            Simbolo s = tabelaSimbolos.get(i);
            if (s.nome.equals(nome) && s.nivelEscopo == 0) return s;
        }
        return null;
    }

    // verifica se o identificador esta declarado no escopo visivel
    private Simbolo verificarDeclaracao(String nome, int pos) throws SemanticError {
        Simbolo s = buscarSimbolo(nome);
        if (s == null) {
            boolean existeEmOutroEscopo = tabelaSimbolos.stream().anyMatch(sim -> sim.nome.equals(nome));
            if (existeEmOutroEscopo) {
                throw new SemanticError("Identificador '" + nome + "' existe mas nao esta visivel no escopo atual (escopo " + pilhaEscopo.peek() + ").", pos);
            }
            throw new SemanticError("Identificador '" + nome + "' nao declarado.", pos);
        }
        return s;
    }

    // verifica a compatibilidade de tipos em uma expressao usando a tabela semantica e retorna o tipo resultante
    private String verificarTipoExpressao(String tipoOp1, String tipoOp2, int operador, Token token) throws SemanticError {
        int t1 = TabelaSemantica.tipoParaIndice(tipoOp1);
        int t2 = TabelaSemantica.tipoParaIndice(tipoOp2);
        if (t1 == TabelaSemantica.ERR || t2 == TabelaSemantica.ERR) {
            throw new SemanticError("Tipo invalido na expressao: " + tipoOp1 + " e " + tipoOp2, token.getPosition());
        }
        int resultado = TabelaSemantica.resultType(t1, t2, operador);
        if (resultado == TabelaSemantica.ERR) {
            throw new SemanticError("Operacao invalida entre tipos: " + tipoOp1 + " e " + tipoOp2, token.getPosition());
        }
        return TabelaSemantica.indiceParaTipo(resultado);
    }

    // verifica a compatibilidade de tipos em uma expressao usando a tabela semantica e retorna o tipo resultante
    private void verificarTipoAtribuicao(String tipoVariavel, String tipoExpressao, int pos) throws SemanticError {
        int tv = TabelaSemantica.tipoParaIndice(tipoVariavel);
        int te = TabelaSemantica.tipoParaIndice(tipoExpressao);
        if (tv == TabelaSemantica.ERR || te == TabelaSemantica.ERR) {
            throw new SemanticError("Tipo invalido na atribuicao de '" + nomeVariavelAtribuicao + "'", pos);
        }
        int resultado = TabelaSemantica.atribType(tv, te);
        if (resultado == TabelaSemantica.WAR) {
            warnings.add("Aviso: atribuicao de " + tipoExpressao + " para " + tipoVariavel + ", possivel perda de precisao. Variavel '" + nomeVariavelAtribuicao + "' na posicao " + pos);
        } else if (resultado == TabelaSemantica.ERR) {
            throw new SemanticError("Atribuicao incompativel: variavel '" + nomeVariavelAtribuicao + "' e do tipo " + tipoVariavel + " mas recebe " + tipoExpressao, pos);
        }
    }
}
