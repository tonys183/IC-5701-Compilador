package edu.ic5701.ast.expr;

import edu.ic5701.ast.Visitor;
import edu.ic5701.ast.stmt.Stmt;
import edu.ic5701.ast.type.Type;
import edu.ic5701.scanner.Token;
import edu.ic5701.sem.table.Symbol;

import java.util.Optional;

public interface Expr extends Stmt {
    <R> R accept(Visitor<R> visitor);
    Token token();
    Type type();
    Symbol symbol();
}
