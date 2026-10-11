package edu.ic5701.error;

import edu.ic5701.scanner.Token;

public interface TokenErrorHandler {
    RuntimeException error(Token token, String template, Object... args);
}
