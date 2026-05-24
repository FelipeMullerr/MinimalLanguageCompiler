package com.minimalide.gals;

import java.util.ArrayList;
import java.util.List;

public class GeradorCodigo {
    private final List<String> secaoData = new ArrayList<>();
    private final List<String> secaoText = new ArrayList<>();

    public static final String TEMP_OP1   = "1000";
    public static final String TEMP_OP2   = "1001";
    public static final String TEMP_ATRIB = "1002";
    public static final String INDR       = "$indr";

    private int proximoTemp = 0;

    public String getTemp() {
        if (proximoTemp == 0) {
            proximoTemp = 1;
            return TEMP_OP1;
        } else {
            proximoTemp = 0;
            return TEMP_OP2;
        }
    }

    public void freeTemp(String temp) {
        proximoTemp = 0;
    }

    public void resetTemps() {
        proximoTemp = 0;
    }

    public void gerarData(List<Simbolo> tabela) {
        secaoData.clear();
        secaoData.add(".data");
        for (Simbolo s : tabela) {
            if (s.nivelEscopo == 0 && s.categoria != Simbolo.Categoria.ROTINA) {
                if (s.categoria == Simbolo.Categoria.VETOR) {
                    StringBuilder valores = new StringBuilder();
                    for (int i = 0; i < s.tamanhoVetor; i++) {
                        valores.append("0");
                        if (i < s.tamanhoVetor - 1) valores.append(",");
                    }
                    secaoData.add("    " + s.nome + ": " + valores);
                } else {
                    secaoData.add("    " + s.nome + ": 0");
                }
            }
        }
    }

    public void gerarText(String instrucao) {
        secaoText.add("    " + instrucao);
    }

    public void emitirRotulo(String rotulo) {
        secaoText.add(rotulo + ":");
    }

    public String getCodigo() {
        StringBuilder sb = new StringBuilder();
        for (String linha : secaoData) sb.append(linha).append("\n");
        sb.append(".text\n");
        for (String linha : secaoText) sb.append(linha).append("\n");
        return sb.toString();
    }
}
