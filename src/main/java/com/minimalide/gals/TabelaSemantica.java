package com.minimalide.gals;

public class TabelaSemantica {

    public static final int ERR = -1;
    public static final int OK_ = 0;
    public static final int WAR = 1;

    public static final int INT = 0;
    public static final int FLO = 1;
    public static final int CHA = 2;
    public static final int STR = 3;
    public static final int BOO = 4;

    public static final int SUM = 0;
    public static final int SUB = 1;
    public static final int MUL = 2;
    public static final int DIV = 3;
    public static final int REL = 4;

    static int expTable[][][] =
    {           /*         INT              */  /*        FLOAT             */  /*         CHAR             */  /*        STRING            */  /*         BOOL             */
    /* INT  */  { {INT,INT,INT,FLO,BOO},        {FLO,FLO,FLO,FLO,BOO},        {ERR,ERR,ERR,ERR,ERR},        {ERR,ERR,ERR,ERR,ERR},        {ERR,ERR,ERR,ERR,ERR} },
    /* FLO  */  { {FLO,FLO,FLO,FLO,BOO},       {FLO,FLO,FLO,FLO,BOO},        {ERR,ERR,ERR,ERR,ERR},        {ERR,ERR,ERR,ERR,ERR},        {ERR,ERR,ERR,ERR,ERR} },
    /* CHA  */  { {ERR,ERR,ERR,ERR,ERR},       {ERR,ERR,ERR,ERR,ERR},         {STR,ERR,ERR,ERR,BOO},        {STR,ERR,ERR,ERR,ERR},        {ERR,ERR,ERR,ERR,ERR} },
    /* STR  */  { {ERR,ERR,ERR,ERR,ERR},       {ERR,ERR,ERR,ERR,ERR},         {STR,ERR,ERR,ERR,ERR},        {STR,ERR,ERR,ERR,BOO},        {ERR,ERR,ERR,ERR,ERR} },
    /* BOO  */  { {ERR,ERR,ERR,ERR,ERR},       {ERR,ERR,ERR,ERR,ERR},         {ERR,ERR,ERR,ERR,ERR},        {ERR,ERR,ERR,ERR,ERR},        {ERR,ERR,ERR,ERR,BOO} }
    };

    static int atribTable[][] = {
    /* INT */ {OK_, WAR, ERR, ERR, ERR},
    /* FLO */ {OK_, OK_, ERR, ERR, ERR},
    /* CHA */ {ERR, ERR, OK_, ERR, ERR},
    /* STR */ {ERR, ERR, OK_, OK_, ERR},
    /* BOO */ {ERR, ERR, ERR, ERR, OK_}
    };

    public static int resultType(int tp1, int tp2, int op) {
        return expTable[tp1][tp2][op];
    }

    public static int atribType(int tp1, int tp2) {
        return atribTable[tp1][tp2];
    }

    public static int tipoParaIndice(String tipo) {
        switch (tipo) {
            case "int":    return INT;
            case "float":  return FLO;
            case "char":   return CHA;
            case "string": return STR;
            case "bool":   return BOO;
            default:       return ERR;
        }
    }

    public static String indiceParaTipo(int indice) {
        switch (indice) {
            case INT: return "int";
            case FLO: return "float";
            case CHA: return "char";
            case STR: return "string";
            case BOO: return "bool";
            default:  return "erro";
        }
    }
}
