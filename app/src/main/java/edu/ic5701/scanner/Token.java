package edu.ic5701.scanner;

public record Token(
        TokenType type,
        String lexeme,
        Integer line,
        Integer offset
) {
}
