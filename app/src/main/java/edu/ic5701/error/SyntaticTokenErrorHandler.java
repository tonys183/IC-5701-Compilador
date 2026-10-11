package edu.ic5701.error;

import edu.ic5701.scanner.Token;

public class SyntaticTokenErrorHandler implements TokenErrorHandler {
    private static final String ERR_PREFIX =
            "[Syntactic error] %s at line %d, column %d: %s";

    @Override
    public RuntimeException error(Token token, String template, Object... args) {
        String detail = String.format(template, args);
        String message = String.format(
                ERR_PREFIX,
                detail,
                token.line(),
                token.offset(),
                token.lexeme()
        );
        return new RuntimeException(message);
    }
}
