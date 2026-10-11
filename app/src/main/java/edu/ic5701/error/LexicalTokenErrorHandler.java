package edu.ic5701.error;

import edu.ic5701.scanner.Token;

public class LexicalTokenErrorHandler implements TokenErrorHandler {

    private static final String ERR_PREFIX =
            "[Lexical error] at line %d, column %d: '%s'";

    @Override
    public RuntimeException error(Token token, String template, Object... args) {
        String message = String.format(
                ERR_PREFIX,
                token.line(),
                token.offset(),
                token.lexeme()
        );
        return new RuntimeException(message);
    }
}
