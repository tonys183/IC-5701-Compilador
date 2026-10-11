package edu.ic5701.ast.type;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.expr.Expr;
import edu.ic5701.scanner.Token;

import java.util.List;
import java.util.Optional;

public record ArrayType(Type baseType, List<Optional<Expr>> dimensions) implements Type {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Token token() {
        return baseType.token();
    }

    @Override
    public Integer dimensionSizeOf() {
        return this.dimensions.size();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return baseType.equals(((ArrayType) o).baseType) && dimensions.size() == ((ArrayType) o).dimensions.size();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
