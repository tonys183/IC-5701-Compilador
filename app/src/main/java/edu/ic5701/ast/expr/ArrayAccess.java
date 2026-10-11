package edu.ic5701.ast.expr;

import edu.ic5701.ast.Visitor;
import edu.ic5701.scanner.Token;
import edu.ic5701.ast.type.Type;
import edu.ic5701.sem.table.Symbol;

public record ArrayAccess(Expr target, Expr index, Type type, Symbol symbol) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Token token() {
        return target.token();
    }

    @Override
    public Symbol symbol() {
        return symbol;
    }

    @Override
    public Type type() {
        return type;
    }

}
