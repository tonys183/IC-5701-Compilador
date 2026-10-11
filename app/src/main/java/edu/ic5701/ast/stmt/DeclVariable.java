package edu.ic5701.ast.stmt;

import edu.ic5701.ast.expr.Expr;
import edu.ic5701.ast.type.Type;
import edu.ic5701.ast.Visitor;
import edu.ic5701.scanner.Token;
import edu.ic5701.sem.table.Symbol;

import java.util.Optional;

public record DeclVariable(Type type, Token name, Optional<Expr> assignValue, Symbol symbol) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
