package edu.ic5701.scanner;

public enum TokenType {
    KEYWORD_PRINT,
    KEYWORD_AND,
    KEYWORD_OR,
    KEYWORD_INT("Int"),
    KEYWORD_STRING("String"),
    KEYWORD_CHAR("Char"),
    KEYWORD_FLOAT("Float"),
    KEYWORD_LIST,
    KEYWORD_IF,
    KEYWORD_ELSE,
    KEYWORD_RETURN,
    KEYWORD_FOR,
    KEYWORD_WHILE,
    KEYWORD_IN,
    KEYWORD_MAIN,
    KEYWORD_POP,
    KEYWORD_PUSH,

    WHITE_SPACE,
    LIT_INT,
    LIT_FLOAT,
    LIT_STRING,
    LIT_CHAR,
    IDENT,
    OP_LE,
    OP_LEQ,
    OP_GE,
    OP_GEQ,
    OP_SUM,
    OP_SUB,
    OP_MUL,
    OP_MOD,
    OP_DIV,
    OP_NEGATION,
    OP_EQU,
    OP_ASSIGN,
    TYPING,
    RANGE,
    PARENT_L,
    PARENT_R,
    BRACKET_L,
    BRACKET_R,
    SQRBRACKET_L,
    SQRBRACKET_R,
    SEMICOLON,
    COMMA,
    DOT,
    EOF,
    NIL;

    private final String lexeme;

    TokenType(String lexeme) {
        this.lexeme = lexeme;
    }

    TokenType() {
        this.lexeme = null;
    }

    public String lexeme() {
        return lexeme;
    }
}
