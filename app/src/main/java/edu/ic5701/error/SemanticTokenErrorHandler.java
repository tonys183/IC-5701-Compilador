package edu.ic5701.error;

import edu.ic5701.scanner.Token;

public class SemanticTokenErrorHandler implements TokenErrorHandler {
    private static final String ERR_PREFIX =
            "[Semantic error] at line %d, offset %d near '%s': %s";

    @Override
    public RuntimeException error(Token token, String template, Object... args) {
        String detail = String.format(template, args);
        String message = String.format(
                ERR_PREFIX,
                token.line(),
                token.offset(),
                token.lexeme(),
                detail
        );
        return new RuntimeException(message);
    }

}
