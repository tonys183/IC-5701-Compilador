package edu.ic5701.ast.expr;

import edu.ic5701.ast.Visitor;
import edu.ic5701.scanner.Token;
import java.util.List;
import edu.ic5701.ast.type.Type;
import edu.ic5701.sem.table.Symbol;

public record FuncCall(Expr name, List<Expr> args, Type type, Symbol symbol) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Token token() {
        return name.token();
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
