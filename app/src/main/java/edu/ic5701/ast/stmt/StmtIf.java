package edu.ic5701.ast.stmt;

import edu.ic5701.ast.expr.Expr;
import edu.ic5701.ast.Visitor;
import java.util.Optional;

public record StmtIf(Expr cond, Block thenBody, Optional<Block> elseBody) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
