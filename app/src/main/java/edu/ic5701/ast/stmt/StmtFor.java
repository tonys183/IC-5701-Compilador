package edu.ic5701.ast.stmt;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.Expr;
import java.util.Optional;

public record StmtFor(DeclVariable iterator, Optional<Expr> list, Block body) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
