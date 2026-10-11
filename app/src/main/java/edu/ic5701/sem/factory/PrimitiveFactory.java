package edu.ic5701.sem.factory;

import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;

public interface PrimitiveFactory {
    PrimitiveType createPrimitive(TokenType type, Token token);
}
