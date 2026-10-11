package edu.ic5701.ast.type;

import edu.ic5701.ast.Visitor;
import edu.ic5701.scanner.Token;

public interface Type {
    <R> R accept(Visitor<R> visitor);
    Token token();
    Integer dimensionSizeOf();
}
