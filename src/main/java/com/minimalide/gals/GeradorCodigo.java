package com.minimalide.gals;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GeradorCodigo {

    private final List<String> secaoData = new ArrayList<>();
    private final List<String> secaoText = new ArrayList<>();

    public static final String TEMP_OP1   = "1000";
    public static final String TEMP_OP2   = "1001";
    public static final String TEMP_ATRIB = "1002";
    public static final String INDR       = "$indr";
    private int proximoTemp = 0;

    public String getTemp() {
        if (proximoTemp == 0) { proximoTemp = 1; return TEMP_OP1; }
        else                  { proximoTemp = 0; return TEMP_OP2; }
    }
    public void freeTemp(String temp) { proximoTemp = 0; }
    public void resetTemps()          { proximoTemp = 0; }

    // rótulos de desvio
    private int contadorRotulo = 0;
    private final Stack<String> pilhaRotulos = new Stack<>();

    public String newRotulo()          { return "R" + (contadorRotulo++); }
    public void   pushRotulo(String r) { pilhaRotulos.push(r); }
    public String popRotulo()          { return pilhaRotulos.isEmpty() ? "R?" : pilhaRotulos.pop(); }

    // operador relacional atual
    private String oprelAtual = null;
    public void   setOprel(String op) { this.oprelAtual = op; }
    public String getOprel()          { return oprelAtual; }

    // Branch que pula o bloco quando a condição é FALSA (if/while/for)
    public String getBranchInverso() {
        if (oprelAtual == null) return "BGE";
        return switch (oprelAtual) {
            case ">"  -> "BLE";
            case "<"  -> "BGE";
            case ">=" -> "BLT";
            case "<=" -> "BGT";
            case "==" -> "BNE";
            case "!=" -> "BOE";
            default   -> "BGE";
        };
    }

    // Branch que volta ao início quando a condição é VERDADEIRA (do-while)
    public String getBranchDireto() {
        if (oprelAtual == null) return "BLT";
        return switch (oprelAtual) {
            case ">"  -> "BGT";
            case "<"  -> "BLT";
            case ">=" -> "BGE";
            case "<=" -> "BLE";
            case "==" -> "BOE";
            case "!=" -> "BNE";
            default   -> "BLT";
        };
    }

    // buffer do pós-operação do for
    private boolean bufferingPostOp = false;
    private final List<String> bufferPostOpAtual = new ArrayList<>();
    private final Stack<List<String>> pilhaPostOps = new Stack<>();

    public void startBufferPostOp() {
        bufferingPostOp = true;
        bufferPostOpAtual.clear();
    }
    public void stopBufferPostOp() {
        bufferingPostOp = false;
        pilhaPostOps.push(new ArrayList<>(bufferPostOpAtual));
        bufferPostOpAtual.clear();
    }
    public void flushPostOp() {
        if (!pilhaPostOps.isEmpty()) {
            for (String linha : pilhaPostOps.pop()) secaoText.add(linha);
        }
    }

    // geração de instruções (respeita o buffer do for)
    public void gerarText(String instrucao) {
        String linha = "    " + instrucao;
        if (bufferingPostOp)  {
            bufferPostOpAtual.add(linha);
        }
        else {
            secaoText.add(linha);
        }
    }

    public void emitirRotulo(String rotulo) {
        String linha = rotulo + ":";
        if (bufferingPostOp) {
            bufferPostOpAtual.add(linha);
        }
        else {
            secaoText.add(linha);
        }
    }

    public void gerarData(List<Simbolo> tabela) {
        secaoData.clear();
        secaoData.add(".data");
        for (Simbolo s : tabela) {
            if (s.nivelEscopo == 0 && s.categoria != Simbolo.Categoria.ROTINA) {
                if (s.categoria == Simbolo.Categoria.VETOR) {
                    StringBuilder v = new StringBuilder();
                    for (int i = 0; i < s.tamanhoVetor; i++) {
                        v.append("0");
                        if (i < s.tamanhoVetor - 1) v.append(",");
                    }
                    secaoData.add("    " + s.nome + ": " + v);
                } else {
                    secaoData.add("    " + s.nome + ": 0");
                }
            }
        }
    }

    public String getCodigo() {
        StringBuilder sb = new StringBuilder();
        for (String linha : secaoData) sb.append(linha).append("\n");
        sb.append(".text\n");
        for (String linha : secaoText) sb.append(linha).append("\n");
        return sb.toString();
    }
}
