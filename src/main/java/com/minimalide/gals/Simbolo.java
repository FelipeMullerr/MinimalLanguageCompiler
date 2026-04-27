package com.minimalide.gals;

import java.util.ArrayList;
import java.util.List;

public class Simbolo {

    public enum Categoria {
        VARIAVEL,
        VETOR,
        PARAMETRO,
        ROTINA,
    }

    public String tipo;
    public String nome;
    public Categoria categoria;
    public int nivelEscopo;
    public boolean usado;
    public boolean inicializado;
    public int tamanhoVetor;

    //public List<String> tiposParametros;

    public Simbolo() {
        this.categoria = Categoria.VARIAVEL;
        this.nivelEscopo = 0;
        this.usado = false;
        this.inicializado = false;
        this.tamanhoVetor = 0;
        //this.tiposParametros = new ArrayList<>();
    }
}
