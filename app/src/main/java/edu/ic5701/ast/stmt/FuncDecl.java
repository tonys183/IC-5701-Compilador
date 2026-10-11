package edu.ic5701.ast.stmt;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.type.Type;
import edu.ic5701.scanner.Token;

public record FuncDecl(Type funcType, ParamDecl param, Token name, Block body) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
