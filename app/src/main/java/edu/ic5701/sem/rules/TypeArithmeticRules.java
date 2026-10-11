package edu.ic5701.sem.rules;

import edu.ic5701.ast.type.Type;
import edu.ic5701.error.TokenErrorHandler;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;
import edu.ic5701.sem.factory.PrimitiveFactory;

public final class TypeArithmeticRules implements ArithmeticRules {

    private static final String ERR_INCOMPATIBLE_LITERAL_OP =
            "The values '%s' and '%s' must be the same type for this operation";

    private final TokenErrorHandler errorHandler;
    private final PrimitiveFactory factory;

    public TypeArithmeticRules(
            final TokenErrorHandler errorHandler,
            final PrimitiveFactory factory
    ) {
        this.errorHandler = errorHandler;
        this.factory = factory;
    }

    @Override
    public Type additive(Type leftType, Type rightType, Token token) {
        if (leftType.equals(rightType)) {
            return leftType;
        }

        boolean intChar = isInt(leftType) && isChar(rightType) || isChar(leftType) && isInt(rightType);
        if (intChar) {
            return factory.createPrimitive(TokenType.KEYWORD_CHAR, token);
        }

        boolean intFloat = isInt(leftType) && isFloat(rightType) || isFloat(leftType) && isInt(rightType);
        if (intFloat) {
            return factory.createPrimitive(TokenType.KEYWORD_FLOAT, token);
        }

        throw errorHandler.error(
                token,
                ERR_INCOMPATIBLE_LITERAL_OP,
                leftType.token().lexeme(),
                rightType.token().lexeme()
        );
    }

    @Override
    public Type multiplicative(Type leftType, Type rightType, Token token) {
        if (leftType.equals(rightType)) {
            return leftType;
        }

        boolean intFloat = isInt(leftType) && isFloat(rightType) || isFloat(leftType) && isInt(rightType);
        if (intFloat) {
            return factory.createPrimitive(TokenType.KEYWORD_FLOAT, token);
        }

        throw errorHandler.error(
                token,
                ERR_INCOMPATIBLE_LITERAL_OP,
                leftType.token().lexeme(),
                rightType.token().lexeme()
        );
    }

    private boolean isInt(Type type) {
        return type.token().type().equals(TokenType.KEYWORD_INT);
    }

    private boolean isFloat(Type type) {
        return type.token().type().equals(TokenType.KEYWORD_FLOAT);
    }

    private boolean isChar(Type type) {
        return type.token().type().equals(TokenType.KEYWORD_CHAR);
    }
}
