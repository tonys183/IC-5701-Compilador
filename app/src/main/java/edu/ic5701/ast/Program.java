package edu.ic5701.ast;

import edu.ic5701.ast.stmt.FuncDecl;

import java.util.List;

public record Program(List<FuncDecl> body) {
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
