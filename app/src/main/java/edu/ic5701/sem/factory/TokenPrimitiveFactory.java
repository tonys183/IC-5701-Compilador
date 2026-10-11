package edu.ic5701.sem.factory;

import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;

public class TokenPrimitiveFactory implements PrimitiveFactory {

    @Override
    public PrimitiveType createPrimitive(TokenType type, Token token) {
        return new PrimitiveType(new Token(type, type.lexeme(), token.line(), token.offset()));
    }
}
