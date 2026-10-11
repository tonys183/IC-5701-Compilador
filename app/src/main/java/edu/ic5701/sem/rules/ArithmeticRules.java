package edu.ic5701.sem.rules;

import edu.ic5701.ast.type.Type;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;

public interface ArithmeticRules {
    Type additive(Type leftType, Type rightType, Token token);
    Type multiplicative(Type leftType, Type rightType, Token token);
}
