package edu.ic5701.ast.stmt;

import edu.ic5701.ast.expr.Expr;
import edu.ic5701.ast.Visitor;

public record StmtReturn(Expr expr) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
