package edu.ic5701.ast.type;

import edu.ic5701.ast.Visitor;
import edu.ic5701.scanner.Token;

public record ListType(Type innerType) implements Type {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Token token() {
        return innerType.token();
    }

    @Override
    public Integer dimensionSizeOf() {
        return 1 + this.innerType().dimensionSizeOf();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
