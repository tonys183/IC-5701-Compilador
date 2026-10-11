package edu.ic5701.ast.expr;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.type.PrimitiveType;
import edu.ic5701.ast.type.Type;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;
import edu.ic5701.sem.table.Symbol;

public record IntLitExpr(Token intToken, int value) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Token token() {
        return intToken;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass();
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public Symbol symbol() {
        return null;
    }

    @Override
    public Type type() {
        return new PrimitiveType(intToken);
    }
}
