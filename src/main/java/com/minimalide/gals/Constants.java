package com.minimalide.gals;

public interface Constants extends ScannerConstants, ParserConstants
{
    int EPSILON  = 0;
    int DOLLAR   = 1;

    int t_COMMENT_LINE = 2;
    int t_COMMENT_BLOCK = 3;
    int t_KEY_IF = 4;
    int t_KEY_THEN = 5;
    int t_KEY_ELSE = 6;
    int t_KEY_END = 7;
    int t_KEY_WHILE = 8;
    int t_KEY_FOR = 9;
    int t_KEY_DO = 10;
    int t_KEY_LET = 11;
    int t_KEY_FN = 12;
    int t_KEY_RET = 13;
    int t_KEY_IN = 14;
    int t_KEY_OUT = 15;
    int t_KEY_INT = 16;
    int t_KEY_FLOAT = 17;
    int t_KEY_STRING = 18;
    int t_KEY_CHAR = 19;
    int t_KEY_BOOL = 20;
    int t_KEY_VOID = 21;
    int t_KEY_TRUE = 22;
    int t_KEY_FALSE = 23;
    int t_OP_ADD = 24;
    int t_OP_SUB = 25;
    int t_OP_MUL = 26;
    int t_OP_DIV = 27;
    int t_OP_MOD = 28;
    int t_OP_ASSIGN = 29;
    int t_OP_INC = 30;
    int t_OP_DEC = 31;
    int t_OP_GE = 32;
    int t_OP_LE = 33;
    int t_OP_EQ = 34;
    int t_OP_NE = 35;
    int t_OP_GT = 36;
    int t_OP_LT = 37;
    int t_OP_AND = 38;
    int t_OP_OR = 39;
    int t_OP_NOT = 40;
    int t_OP_SHIFT_LEFT = 41;
    int t_OP_SHIFT_RIGHT = 42;
    int t_OP_BIT_AND = 43;
    int t_OP_BIT_OR = 44;
    int t_OP_BIT_NOT = 45;
    int t_OP_BIT_XOR = 46;
    int t_INT = 47;
    int t_FLOAT = 48;
    int t_CHAR = 49;
    int t_STRING = 50;
    int t_DOT = 51;
    int t_SEMICOLON = 52;
    int t_COLON = 53;
    int t_COMMA = 54;
    int t_LPAREN = 55;
    int t_RPAREN = 56;
    int t_LBRACKET = 57;
    int t_RBRACKET = 58;
    int t_LBRACE = 59;
    int t_RBRACE = 60;
    int t_IDENT = 61;

}
