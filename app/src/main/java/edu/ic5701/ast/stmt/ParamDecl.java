package edu.ic5701.ast.stmt;

import edu.ic5701.ast.Visitor;
import java.util.List;

public record ParamDecl(List<DeclVariable> params) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
