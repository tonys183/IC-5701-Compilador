package edu.ic5701.ast.type;

import edu.ic5701.ast.Visitor;
import edu.ic5701.scanner.Token;
import edu.ic5701.scanner.TokenType;

public record PrimitiveType(Token token) implements Type {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return this.token.type().equals(((PrimitiveType) o).token.type());
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public Integer dimensionSizeOf() {
        return token.type().equals(TokenType.KEYWORD_STRING) ? 1 : 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
