package edu.ic5701.ast.stmt;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.Expr;

public record StmtExpr(Expr expr) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
