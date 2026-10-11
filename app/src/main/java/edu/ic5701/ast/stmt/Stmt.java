package edu.ic5701.ast.stmt;

import edu.ic5701.ast.Visitor;

public interface Stmt {
    <R> R accept(Visitor<R> visitor);
}
